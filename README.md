# Locus API

Back-end da plataforma **Locus**, uma aplicação voltada para destinos, viagens, imóveis, reservas e experiências turísticas.
O projeto foi desenvolvido com **Java 21**, **Spring Boot**, **PostgreSQL**, **Redis**, **AWS S3**, **Spring Security**, **JWT**, **Spring AI** e integração com a API do **Gemini Flash** para enriquecimento automático de destinos turísticos.

## Sobre o projeto

O **Locus API** é uma aplicação back-end construída para gerenciar funcionalidades centrais de uma plataforma de turismo e aluguel de imóveis, incluindo autenticação, usuários, destinos, propriedades, reservas, avaliações e upload de arquivos.

Um dos principais diferenciais do projeto é o uso de **IA Generativa** para enriquecer automaticamente os dados de destinos cadastrados. Ao criar um destino informando país e cidade, a aplicação publica um evento interno que é consumido por um serviço de IA. Esse serviço utiliza o **Spring AI** integrado à API do **Gemini Flash** para buscar e gerar informações sobre os pontos turísticos mais famosos daquele destino. Após o processamento, um novo evento é publicado e o módulo responsável por destinos consome os dados para persistência no banco.

## Principais funcionalidades

* Cadastro e autenticação de usuários.
* Autenticação baseada em JWT.
* Controle de segurança com Spring Security.
* Gerenciamento de destinos turísticos.
* Enriquecimento automático de destinos com IA Generativa.
* Geração de pontos turísticos com Gemini Flash via Spring AI.
* Gerenciamento de imóveis/propriedades.
* Upload e armazenamento de arquivos com AWS S3.
* Gerenciamento de reservas.
* Sistema de avaliações.
* Persistência relacional com PostgreSQL.
* Cache e apoio a fluxos temporários com Redis.
* Documentação de API com OpenAPI/Scalar.
* Testes automatizados com suporte a Testcontainers.

## Fluxo de IA Generativa

O fluxo de enriquecimento de destinos funciona de forma orientada a eventos:

```text
1. Usuário cria um destino informando país e cidade
        ↓
2. A aplicação publica um evento interno de destino criado
        ↓
3. O service de IA consome o evento
        ↓
4. O Spring AI chama a API do Gemini Flash
        ↓
5. A IA retorna os pontos turísticos mais famosos do destino
        ↓
6. Um novo evento é publicado com os dados processados
        ↓
7. O módulo de destino consome o evento e persiste as informações
```

Esse fluxo separa responsabilidades entre criação do destino, processamento com IA e persistência dos dados enriquecidos, evitando acoplamento direto entre a regra de negócio principal e o serviço de IA.

## Stack utilizada

### Back-end

* Java 21
* Spring Boot 3.3
* Spring Web
* Spring Validation
* Spring Security
* Spring Data JPA
* Spring Mail
* Spring OAuth2 Client

### Banco de dados e cache

* PostgreSQL
* Redis
* JPA/Hibernate

### Inteligência Artificial

* Spring AI
* Google GenAI
* Gemini Flash

### Segurança

* JWT
* Spring Security
* Cookies HTTP Only
* OAuth2 Client

### Cloud e arquivos

* AWS S3
* AWS SDK for Java

### Documentação

* OpenAPI
* Springdoc OpenAPI
* Scalar

### Testes

* JUnit
* Spring Boot Test
* Spring Security Test
* Testcontainers
* Testcontainers PostgreSQL
* Testcontainers Redis

### Ferramentas

* Maven
* Docker
* Docker Compose
* Lombok

## Arquitetura

O projeto segue uma organização baseada em responsabilidades, separando camadas e serviços de domínio. A estrutura foi pensada para facilitar manutenção, testes e evolução da aplicação.

Principais decisões arquiteturais:

* Separação entre controllers, services, repositories e entidades.
* Uso de eventos internos para desacoplar fluxos de negócio.
* Service dedicado para integração com IA.
* Persistência relacional com JPA/Hibernate.
* Uso de Redis para recursos temporários/cache.
* Documentação automática dos contratos da API.
* Testes com infraestrutura isolada via Testcontainers.

## Exemplo de caso de uso com IA

Ao cadastrar um destino como:

```json
{
  "country": "Brasil",
  "city": "Rio de Janeiro"
}
```

A aplicação pode acionar o fluxo de IA para gerar pontos turísticos como:

```json
[
  "Cristo Redentor",
  "Pão de Açúcar",
  "Praia de Copacabana",
  "Museu do Amanhã",
  "Escadaria Selarón"
]
```

Esses dados são processados e persistidos automaticamente, enriquecendo o destino sem exigir cadastro manual de cada ponto turístico.

## Pré-requisitos

Antes de executar o projeto, é necessário ter instalado:

* Java 21+
* Maven
* Docker
* Docker Compose
* PostgreSQL
* Redis
* Conta ou chave de API para Gemini/Google GenAI
* Credenciais da AWS S3, caso utilize upload de arquivos

## Variáveis de ambiente

Crie um arquivo `.env` ou configure as variáveis no ambiente de execução.

Exemplo:

```env
# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/locus
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=sua_chave_secreta
JWT_EXPIRATION=86400000

# Gemini / Google GenAI
GEMINI_API_KEY=sua_chave_da_api_gemini
SPRING_AI_MODEL=gemini-flash

# AWS S3
AWS_ACCESS_KEY_ID=sua_access_key
AWS_SECRET_ACCESS_KEY=sua_secret_key
AWS_REGION=sa-east-1
AWS_S3_BUCKET_NAME=nome_do_bucket

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email
MAIL_PASSWORD=sua_senha_de_app
```

Atenção: não versionar arquivos `.env`, chaves privadas, tokens ou credenciais reais.

## Como executar localmente

Clone o repositório:

```bash
git clone https://github.com/GiovanneVieira/Locus-Back.git
cd Locus-Back
```

Instale as dependências e compile o projeto:

```bash
mvn clean install
```

Execute a aplicação:

```bash
mvn spring-boot:run
```

A API ficará disponível, por padrão, em:

```text
http://localhost:8080
```

## Executando com Docker

Caso o projeto esteja configurado com Docker/Docker Compose, suba os serviços necessários:

```bash
docker compose up -d
```

Depois execute a aplicação:

```bash
mvn spring-boot:run
```

## Documentação da API

A documentação interativa da API pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

Ou, caso esteja utilizando Scalar:

```text
http://localhost:8080/scalar
```

## Testes

Para executar os testes:

```bash
mvn test
```

O projeto utiliza suporte a **Testcontainers**, permitindo testar integrações com serviços como PostgreSQL e Redis em ambiente isolado.

## Estrutura esperada do projeto

```text
src/
 └── main/
     ├── java/
     │   └── com/project/locusapi/
     │       ├── controllers/
     │       ├── services/
     │       ├── repositories/
     │       ├── entities/
     │       ├── dtos/
     │       ├── events/
     │       ├── security/
     │       ├── config/
     │       └── integrations/
     └── resources/
         ├── application.properties
         └── application.yml
```

## Destaques técnicos

* Integração de IA Generativa em um fluxo real de aplicação back-end.
* Uso de arquitetura orientada a eventos para desacoplar processamento assíncrono.
* Aplicação de Spring AI com Gemini Flash para enriquecimento de dados.
* Segurança com Spring Security e JWT.
* Persistência com PostgreSQL e JPA/Hibernate.
* Uso de Redis para suporte a cache e dados temporários.
* Integração com AWS S3 para armazenamento de arquivos.
* Documentação técnica com OpenAPI/Scalar.
* Testes com Testcontainers para maior confiabilidade em integrações.

## Possíveis melhorias futuras

* Adicionar fila/mensageria externa para processamento assíncrono.
* Criar retry e fallback para falhas na chamada da IA.
* Adicionar observabilidade com logs estruturados e métricas.
* Implementar rate limit para chamadas à IA.
* Melhorar cobertura de testes unitários e de integração.
* Adicionar pipeline de CI/CD.
* Criar ambiente de deploy em cloud.
* Versionar documentação de endpoints.
* Adicionar monitoramento de custos da API de IA.

## Autor

Desenvolvido por **Giovanne Vieira de Queiroz**.

* GitHub: github.com/GiovanneVieira
* LinkedIn: linkedin.com/in/giovanne-vieira-de-queiroz-77259b277

