# AGENTS.MD - Diretrizes de Arquitetura e Padrões SOLID

Este documento define os padrões arquiteturais, decisões de design e regras de engenharia que governam a evolução do ecossistema backend do **Locus**. Ele serve como bússola para agentes de IA e engenheiros seniores na manutenção, refatoração e extensão da plataforma.

---

## 1. Panorama Geral da Base de Código

A API do Locus está construída sobre o ecossistema **Spring Boot 3.3** e **Java 21**. A estrutura de pacotes atual divide responsabilidades entre:
- **`controller`**: Camada de entrada HTTP/RESTful.
- **`service`**: Camada de orquestração de regras de negócio.
- **`repository`**: Abstração de persistência via Spring Data JPA.
- **`model` / `domain`**: Representação de entidades relacionais e Value Objects.
- **`mapper`**: Isolamento de conversão entre DTOs e Modelos.

---

## 2. Aplicação Prática dos Princípios SOLID

A base de código demonstra um excelente nível de maturidade técnica, aplicando diversos padrões de design avançados. Abaixo está a análise detalhada de cada princípio com base no estado atual do código.

### 🧱 S - Single Responsibility Principle (SRP)
*O princípio da responsabilidade única dita que uma classe deve ter um, e apenas um, motivo para mudar.*

* **Pontos Fortes Identificados:**
    * **`S3Service`**: Focado estritamente na infraestrutura física de upload, download e gerenciamento de arquivos no AWS S3.
    * **`OTPService`**: Centraliza apenas a lógica de geração, expiração e validação de tokens temporários utilizando Redis.
    * **`GlobalExceptionHandler`**: Isola todo o tratamento de exceções HTTP, validações de DTOs e erros de persistência fora dos Controllers.
* **Oportunidades de Refatoração:**
    * **`UserModel`**: Atualmente acumula a função de Entidade JPA Relacional e as obrigações da interface `UserDetails` do Spring Security. Para blindar o modelo de domínio contra mudanças no framework de segurança, o ideal é criar uma classe separada (ex: `UserPrincipal implements UserDetails`) que encapsule o `UserModel`.
    * **`AuthService`**: Acumula múltiplas responsabilidades (fluxo de registro, login local, login social via OAuth2, rotação de refresh tokens e gerenciamento manual de HTTP cookies). Recomenda-se mover a lógica de criação de cookies para uma classe auxiliar de infraestrutura HTTP.

### 🔌 O - Open/Closed Principle (OCP)
*Classes devem estar abertas para extensão, mas fechadas para modificação.*

* **Excelência Arquitetural na Codebase:**
    * O projeto utiliza com maestria o **Strategy Pattern** combinado com a injeção polimórfica do Spring. O componente `AddressService` recebe dinamicamente uma lista de estratégias (`List<AddressStrategy>`) e as mapeia em um dicionário imutável indexado pelo `AddressType`:
        ```java
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AddressStrategy::getSupportedType, Function.identity()));
        ```
    * Graças a isso, novos tipos de endereços podem ser criados simplesmente adicionando uma nova classe que implemente `AddressStrategy`, sem alterar uma única linha de código do `AddressService` básico.
    * O mesmo padrão inteligente é replicado no `AddressMapper` (utilizando uma lista de `TypeMapper`) e no `CustomOAuth2SuccessHandler` (utilizando `List<OAuth2UserExtractor>`).

### 📐 L - Liskov Substitution Principle (LSP)
*Subclasses devem ser substituíveis por suas superclasses sem quebrar o comportamento do sistema.*

* **Análise de Implementação:**
    * A herança em `Address`, mapeada através de `@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`, permite que `PersonalAddressModel` e `RentableAddressModel` se comportem de maneira previsível.
* **Alerta de Vazamento de Abstração ("Code Smell"):**
    * Em `RentableAddressStrategy`, identificamos um desvio conceitual do LSP ao lidar com os parâmetros de entrada:
        ```java
        if (dto instanceof RentableAddressRequestDTO rentableDto) { ... }
        ```
    * O uso de `instanceof` para forçar um downcast indica que a interface ou classe abstrata mãe (`AddressRequestDTO`) não definiu um contrato robusto o suficiente, ou que o método aceita um tipo genérico demais para sua real necessidade.
    * **Diretriz:** Evite assinaturas genéricas que exijam validações do tipo `instanceof`. Separe os contratos de serviço quando os fluxos de dados de entrada divergirem profundamente.

### ✂️ I - Interface Segregation Principle (ISP)
*Clientes não devem ser forçados a depender de interfaces que não usam.*

* **Pontos Fortes Identificados:**
    * A interface `CommunicationManager` cumpre perfeitamente o ISP ao declarar apenas o método essencial `sendMsg(String to, String subject, String text)`, permitindo que diferentes provedores (E-mail, SMS, WhatsApp) o implementem de maneira limpa.
    * `BaseFileRepository` atua como um repositório genérico intermediário anotado com `@NoRepositoryBean`, fornecendo o método `findByS3Key` sob demanda apenas para os tipos necessários.

### 🔄 D - Dependency Inversion Principle (DIP)
*Dependa de abstrações, não de implementações concretas.*

* **Pontos Fortes Identificados:**
    * Uso consistente de injeção de dependência via construtor, facilitando testes unitários isolados e stubs (como visto em `AuthServiceTest` e `UserServiceTest`).
    * Utilização de abstrações de alto nível, como `PasswordEncoder` e `StringRedisTemplate`, em vez de acoplamento com classes de baixo nível.
* **Diretriz para Evolução:**
    * Sempre que um serviço core (como `UserService` ou `RentalService`) for consumido por outra classe de negócio, avalie a criação de uma interface de contrato (ex: `IUserService`). Isso desacopla totalmente a orquestração e habilita o uso simplificado de padrões como *Decorators* (ex: para caching com Redis) sem alterar o core business.

---

## 3. Padrões de Domínio Premium (Value Objects)

A aplicação apresenta uma excelente prática de DDD (*Domain-Driven Design*) ao encapsular dados complexos em **Java Records** atuando como **Value Objects**:
1.  **`CEP`**: Valida a estrutura através de expressões regulares, remove caracteres não numéricos automaticamente no construtor padrão e garante a formatação consistente (`12345-678`). Integra-se nativamente ao banco via `CepConverter`.
2.  **`CPF`**: Realiza validação matemática robusta através dos dígitos verificadores e rejeita sequências repetidas inválidas.

---

## 4. Guia de Implementação para Novos Agentes / Desenvolvedores

Ao criar ou expandir código nesta API, siga rigorosamente as seguintes regras estruturais:

### 4.1. Fluxo de Persistência e Ciclo de Vida de Uploads
* **Fase 1 (Upload Independente):** Imagens e arquivos devem ser enviados primeiramente pelo `RentableAddressImageController`. Eles entram no banco de dados como metadados "órfãos" (com relacionamento `address_id = null`).
* **Fase 2 (Vínculo Definitivo):** No momento da criação ou atualização da entidade de negócio, o serviço correspondente deve invocar o método `bindImagesToAddress` para amarrar os UUIDs previamente enviados ao registro recém-criado.

### 4.2. Paginação Segura e Performance de RAM
* Nunca retorne listas brutas (`List<T>`) para consultas públicas ou de grande volume. Sempre utilize as abstrações `Pageable` e `Page<T>` do Spring Data.
* Ao realizar o streaming de arquivos salvos na nuvem (S3), utilize diretamente o retorno por `InputStream` envelopado em um `InputStreamResource`, garantindo consumo sob demanda e evitando estouros de memória na JVM (*Heap Memory Overload*).

### 4.3. Convenções de Código e Mapeamento
* **Uso do Lombok**: Utilize as anotações com critério. Em modelos abstratos com herança, utilize `@SuperBuilder` nas pontas para garantir a fluidez na criação dos objetos pelas estratégias.
* **Transações**: Métodos de escrita, deleção ou múltiplas interações com bancos relacionais devem conter `@Transactional`. Métodos puramente consultivos devem utilizar `@Transactional(readOnly = true)` para otimização de leitura e liberação rápida de conexões no Pool.