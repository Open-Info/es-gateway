# es-gateway
Gateway for experimentation with the Etherscan API

## Environment Variables
| Name        | Description                         | Required | Default   |
|-------------|-------------------------------------|----------|-----------|
| ES_API_KEY  | Key for the Etherscan API           | true     | None      |
| SERVER_PORT | Port that the server is hosted with | false    | 80        |
| SERVER_HOST | Host that the server is hosted with | false    | `0.0.0.0` |

## Usage
### Local
- Install `Java 17 JRE` and [sbt](https://www.scala-sbt.org/download.html)
- Set the relevant ENV variables
- Run the server using `sbt run`

### Docker
- Install `Java 17 JRE`, [sbt](https://www.scala-sbt.org/download.html) and Docker
- Run `sbt stage` to compile and stage the application
- Run `docker build -t es-gateway:latest .` to build the and publish the image
- Run the image with `docker run --rm -p 80:80 -e ES_API_KEY=<your-etherscan-key> <image-id>`
