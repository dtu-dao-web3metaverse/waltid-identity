# walt.id Identity

## Credentials

### (to be) Supported combinations:

| Credential type | Signature type | Notes                                               |
|-----------------|----------------|-----------------------------------------------------|
| W3C VC          | JOSE (JWT/JWS) | Simplest format to start                            |
| SD-JWT          | JOSE (JWT/JWS) | Selective disclosure                                |
| W3C VC          | JSON-LD / Soon | Signature type not recommended for new applications |
| mdoc            | COSE1          | Supports mDL & mID                                  |

## walt.id CoreCrypto

## Supported

### Platforms available:

- Java: JVM
- JS: Node.js or WebCrypto
- Native: libsodium & OpenSSL (todo)
- WebAssembly (WASM): (todo)

### Signature schemes available:

| Type  |   ECDSA   | JOSE ID | Description                                                     |
|:-----:|:---------:|:-------:|:----------------------------------------------------------------|
| EdDSA |  Ed25519  |  EdDSA  | EdDSA + Curve25519                                              |
| ECDSA | secp256r1 |  ES256  | ECDSA + SECG curve secp256r1 ("NIST P-256")                     |
| ECDSA | secp256k1 | ES256K  | ECDSA + SECG curve secp256k1 (Koblitz curve as used in Bitcoin) |
|  RSA  |    RSA    |  RS256  | RSA                                                             |

### Compatibility matrix:

#### JWS (recommended)

| Algorithm | JVM provider |   JS provider / platform    |
|:---------:|:------------:|:---------------------------:|
|   EdDSA   | Nimbus JOSE  |       jose / Node.js        |
|   ES256   | Nimbus JOSE  | jose / Node.js & Web Crypto |
|  ES256K   | Nimbus JOSE  |       jose / Node.js        |
|   RS256   | Nimbus JOSE  | jose / Node.js & Web Crypto |

#### LD Signatures (happy to add upon request - office@walt.id)

|            Suite            |    JVM provider    |    JS provider    |
|:---------------------------:|:------------------:|:-----------------:|
|    Ed25519Signature2018     | ld-signatures-java |                   |
|    Ed25519Signature2020     | ld-signatures-java | jsonld-signatures |
| EcdsaSecp256k1Signature2019 | ld-signatures-java |                   |
|      RsaSignature2018       | ld-signatures-java |                   |
|    JsonWebSignature2020     | ld-signatures-java |                   |

## Docker container builds:

```shell
docker build -t waltid/issuer -f docker/issuer.Dockerfile .
docker run -p 7000:7000 waltid/issuer --webHost=0.0.0.0 --webPort=7000 --baseUrl=http://localhost:7000
```

```shell
docker build -t waltid/verifier -f docker/verifier.Dockerfile .
docker run -p 7001:7001 waltid/verifier --webHost=0.0.0.0 --webPort=7001 --baseUrl=http://localhost:7001
```

### (Optional) Setup Vault

#### apt

```shell
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo
tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install vault
```

```shell
vault server -dev -dev-root-token-id="dev-only-token"
```
#### Docker

```shell
docker run -p 8200:8200 --cap-add=IPC_LOCK -e VAULT_DEV_ROOT_TOKEN_ID=myroot -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 hashicorp/vault
```
