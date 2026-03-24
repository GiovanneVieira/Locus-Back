# Como compilar

Criar um ```.env``` no diretorio local com as seguintes credenciais:

```.env
RDS_HOST=locus-test-db.c32wisquotiu.us-east-2.rds.amazonaws.com
DB_NAME=locustestdb
DB_USERNAME=seu-usuario
DB_PASSWORD=engsoftware
RDS_PORT=5432
```

- Usuarios
  - gustavocamargo
  - gustavochampam
- Senha
  - engsoftware 

Após inserir as credenciais basta rodar ```docker compose up --watch``` para que a imagem seja rebuildada sempre que houver uma alteração e um container seja executado automaticamente.
