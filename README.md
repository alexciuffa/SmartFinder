# SmartFinder
Um app que permite identificar se uma pessoa é ou não procurada pela polícia por meio de uma foto.

## Introdução
O arquivo está dividido em cinco diretórios principais:

```
    .
    facenet_api_android
    ├── Readme.md        # Descrição do projeto e demais informações
    ├── API              # contém um servidor que roda uma API para realizar a comunicação entre o aplicativo e o banco de dados
    ├── Dataset          # contém as fotos e o banco de dados no formato sqlite
    ├── Model            # contém tudo relacionado aos os modelos de machine learning utilizados
    ├── Camera2_v04      # contém tudo relacionado a aplicação Android
    └── Venvs            # contém os ambientes virtuais utilizados no projeto
```

## API
Este é o sistema backend da aplicação do SmartFinder, desenvolvida em Python com Flask, responsável por receber um request da aplicação móvel (aplicativo) com um embedding e verificar se existe um match no banco da dados.

### Ativar o servidor
Dentro do diretório `API/server/` executar o comando:

`$ sudo ../../venvs/server_env/bin/python backend_app.py`

### Caminhos (routes)
 - GET - `/get_people`: retorna todas as pessoas cadastradas no banco de dados
 - GET - `/get_images`: retorna todas as imagens cadastradas no banco de dados
 - GET - `/get_requests`: retorna todos os requests cadastradas no banco de dados
 - GET - `/get_matches`: retorna todos os matchs cadastradas no banco de dados
 - GET - `/time`: retorna o horário atual (puramente para teste de conexão)
 - POST - `/get_wanted_people`: recebe uma latitude (lat), uma longitude (lon) e um embedding e retorna os matches

### Testar o backend
É possível testar o backend pelo arquivo `test_back.py`

## Dataset
Este é banco de dados utilizado. Ele é gerado com os comandos da pasta `scripts`, e os databases gerados estão na pasta `database`. Os scripts utilizam Python e a biblioteca `sqlite3`.

### Database
Os bancos gerados ficam em: `database/`. Inicialmente teremos um arquivo `SmartFinder.db` que contém as tabelas:
 - person
 - image
 - request
 - match

Para apagar, basta fazer: `$ rm -rf SmartFinder.db`. Ou seja, é só deletar o arquivo mesmo.

### Scripts
As funções foram inspiradas no tutorial de sqlite3 do site: https://www.sqlitetutorial.net/sqlite-python/. Este outro site tem uma boa documentação: https://www.tutorialspoint.com/sqlite/sqlite_python.htm.

Para gerar o banco de dados, basta rodar o arquivo `create_db.py` para criar o arquivo `SmartFinder.db` com as tabelas e depois `populate_db.py` para inserir dados de pessoas e imagens a partir dos dados em `data/`. É necessário usar o venv `tf_2` para popular o banco:

`$ source ../../venvs/tf_2/bin/activat`

`$ python create_db.py`

`$ python populate_db.py`

## Model

## Venvs
Existem 3 venvs:

 - db_env: possui apenas o python3.7
 - server_env: possui python3.7 com as bibliotecas Numpy e Flask
 - tf_2: possui python3.7 com as bibliotecas Numpy, Pandas, TensorFlow
