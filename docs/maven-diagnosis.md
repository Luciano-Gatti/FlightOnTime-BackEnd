# Maven Diagnosis

## 1) Comandos ejecutados y outputs

## Módulo: `Prediction-App`
### Comando: `mvn -v`
```text
Apache Maven 3.9.10 (5f519b97e944483d878815739f519b2eade0a91d)
Maven home: /root/.local/share/mise/installs/maven/3.9.10/apache-maven-3.9.10
Java version: 25.0.1, vendor: Oracle Corporation, runtime: /root/.local/share/mise/installs/java/25.0.1
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.12.47", arch: "amd64", family: "unix"
```

### Comando: `mvn -X -e -U -DskipTests package`
```text
[DEBUG] Reading global settings from /root/.local/share/mise/installs/maven/3.9.10/apache-maven-3.9.10/conf/settings.xml
[DEBUG] Reading user settings from /root/.m2/settings.xml
[DEBUG] Resolving artifact org.springframework.boot:spring-boot-starter-parent:pom:4.0.2 from [central (https://repo.maven.apache.org/maven2, default, releases)]
[DEBUG] Using connector BasicRepositoryConnector with priority 0.0 for https://repo.maven.apache.org/maven2 via proxy:8080
Downloading from central: https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/4.0.2/spring-boot-starter-parent-4.0.2.pom
[FATAL] Non-resolvable parent POM for com.flightontime:app_predictor:0.0.1-SNAPSHOT: ... Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:4.0.2 from/to central (https://repo.maven.apache.org/maven2): status code: 403, reason phrase: Forbidden (403)
Caused by: org.apache.http.client.HttpResponseException: status code: 403, reason phrase: Forbidden (403)
```

---

## Módulo: `flights-predictor-test`
### Comando: `mvn -v`
```text
Apache Maven 3.9.10 (5f519b97e944483d878815739f519b2eade0a91d)
Maven home: /root/.local/share/mise/installs/maven/3.9.10/apache-maven-3.9.10
Java version: 25.0.1, vendor: Oracle Corporation, runtime: /root/.local/share/mise/installs/java/25.0.1
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.12.47", arch: "amd64", family: "unix"
```

### Comando: `mvn -X -e -U -DskipTests package`
```text
[DEBUG] Reading global settings from /root/.local/share/mise/installs/maven/3.9.10/apache-maven-3.9.10/conf/settings.xml
[DEBUG] Reading user settings from /root/.m2/settings.xml
[DEBUG] Resolving artifact org.springframework.boot:spring-boot-starter-parent:pom:3.5.9 from [central (https://repo.maven.apache.org/maven2, default, releases)]
[DEBUG] Using connector BasicRepositoryConnector with priority 0.0 for https://repo.maven.apache.org/maven2 via proxy:8080
Downloading from central: https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.5.9/spring-boot-starter-parent-3.5.9.pom
[FATAL] Non-resolvable parent POM for com.flightspredictor:flights:0.0.1-SNAPSHOT: ... Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.5.9 from/to central (https://repo.maven.apache.org/maven2): status code: 403, reason phrase: Forbidden (403)
Caused by: org.apache.http.client.HttpResponseException: status code: 403, reason phrase: Forbidden (403)
```

---

## 2) Identificación pedida

## URL exacta que Maven intenta bajar cuando falla
- `Prediction-App`:
  - Artifact: `org.springframework.boot:spring-boot-starter-parent:pom:4.0.2`
  - Repo: `central (https://repo.maven.apache.org/maven2)`
  - URL exacta: `https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/4.0.2/spring-boot-starter-parent-4.0.2.pom`
- `flights-predictor-test`:
  - Artifact: `org.springframework.boot:spring-boot-starter-parent:pom:3.5.9`
  - Repo: `central (https://repo.maven.apache.org/maven2)`
  - URL exacta: `https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.5.9/spring-boot-starter-parent-3.5.9.pom`

## Tipo de error HTTP
- En ambos módulos: **403 Forbidden**.
- No se observaron 401, 404 ni timeout en estos intentos.

## Proxy / mirror / settings involucrado
- Maven lee:
  - Global settings: `/root/.local/share/mise/installs/maven/3.9.10/apache-maven-3.9.10/conf/settings.xml`
  - User settings: `/root/.m2/settings.xml`
- User settings encontrado (activo):
```xml
<settings>
    <proxies>
        <proxy>
            <id>default</id>
            <active>true</active>
            <protocol>http</protocol>
            <host>proxy</host>
            <port>8080</port>
            <nonProxyHosts>localhost|127.*|*.local</nonProxyHosts>
        </proxy>
    </proxies>
</settings>
```
- En debug Maven aparece explícitamente uso de proxy al resolver central:
  - `Using connector ... for https://repo.maven.apache.org/maven2 via proxy:8080`
- No se detectó mirror custom activo en user settings. El global contiene entradas template/comentadas de ejemplo.

---

## 3) Conclusión de diagnóstico
La falla de resolución del parent POM no parece ser de coordenadas inválidas en POM sino de acceso HTTP al repositorio remoto (central) desde el entorno actual, con tráfico saliendo por proxy (`proxy:8080`) y respuesta **403 Forbidden** para los POM parent solicitados.
