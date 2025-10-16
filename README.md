# Projeto de Colaboração LaTeX

Uma breve descrição do seu projeto, explicando seu objetivo e funcionalidades principais.

## Pré-requisitos

Antes de começar, você precisará ter as seguintes ferramentas instaladas em seu ambiente:

* [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/)
* [Apache Maven](https://maven.apache.org/download.cgi)

## Como Rodar o Projeto

Para executar esta aplicação, você precisará de dois terminais abertos. Siga os passos abaixo.

---

### **Terminal 1: Executando o Servidor**

Neste terminal, vamos compilar e iniciar o servidor Java.

1.  **Compile o código do Servidor:**
    * Este comando compila o arquivo `Server.java`.

    ```bash
    javac Server.java
    ```

2.  **Compile o código do Cliente (opcional neste terminal):**
    * Este comando compila o arquivo `Client.java`.

    ```bash
    javac Client.java
    ```

3.  **Inicie o Servidor:**
    * Este comando executa a classe `Server`, que ficará aguardando conexões dos clientes.

    ```bash
    java Server
    ```

O servidor estará em execução e pronto para receber conexões.

---

### **Terminal 2: Executando o Cliente**

Neste terminal, vamos compilar e executar a aplicação cliente usando o Maven.

1.  **Navegue até o diretório do cliente:**
    * Certifique-se de que você está no diretório correto do projeto cliente.

    ```bash
    cd server
    ```

2.  **Limpe o projeto com Maven:**
    * O comando `clean` remove os artefatos de builds anteriores.

    ```bash
    mvn clean install
    ```

3.  **Empacote a aplicação:**
    * O comando `package` compila o código e o empacota em um arquivo `.jar` executável.

    ```bash
    mvn clean package
    ```

4.  **Execute a aplicação cliente:**
    * Este comando inicia o cliente a partir do arquivo `.jar` gerado, que se conectará ao servidor.

    ```bash
    java -jar target/latex-collaboration-client-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

Agora, a aplicação cliente deve estar rodando e conectada ao servidor.
