# es-gateway
Gateway for experimentation with the Etherscan API

## Environment Variables
| Name        | Description                         | Required | Default     |
|-------------|-------------------------------------|----------|-------------|
| ES_API_KEY  | Key for the Etherscan API           | true     | None        |
| SERVER_PORT | Port that the server is hosted with | false    | 80          |
| SERVER_HOST | Host that the server is hosted with | false    | `localhost` |

## Usage
- Ensure that you have both a [JDK](https://adoptium.net/download/) and [sbt](https://www.scala-sbt.org/download.html) installed
- Ensure that you have set the `ES_API_KEY` variable on your system
- Run the server using `sbt run`

## Docker
- To build a local docker image, all you need to do is have [sbt](https://www.scala-sbt.org/download.html) installed and run `sbt docker:publishLocal` which will build
and publish an image to your local repo.
- To use the image simply run
`docker run -p 80:80 -e "ES_API_KEY=<your-etherscan-key>" <image-id>`
