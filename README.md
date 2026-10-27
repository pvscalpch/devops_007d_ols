# DevOps 007D
**Integrantes:** Cristian Bravo y Pascal Pacheco
## Descripción

Este repositorio contiene el microservicio `ms-auth-user` del proyecto CarMeet.
El microservicio se utiliza como base para implementar un flujo de trabajo DevOps utilizando Git, GitHub y GitHub Actions.

**Tecnologías principales:** Java 17, Spring Boot 4, Maven, MySQL, Flyway, Git, GitHub, GitHub Actions.
---
## 1. Modelos de ramificación evaluados
Antes de definir la estrategia del equipo se compararon tres modelos de ramificación de uso extendido en entornos colaborativos en la nube:
| Modelo | Ramas que utiliza | Aplicabilidad | Limitaciones |
|---|---|---|---|
| **GitFlow** | `main`, `develop`, `feature/*`, `release/*`, `hotfix/*` | Proyectos con versiones formales, varios desarrolladores en paralelo y necesidad de separar lo estable de lo que está en desarrollo | Las ramas viven mucho tiempo; si se despliega muy seguido, los merges se vuelven pesados |
| **GitHub Flow** | `main` + ramas cortas de feature | Despliegue continuo desde `main`, donde cada Pull Request aprobado llega directo a producción | No contempla varias versiones vivas a la vez ni entornos de QA separados |
| **Trunk-Based Development** | Una sola rama principal, ramas de horas | Equipos con integración continua madura, feature flags y entregas varias veces al día | Exige pruebas automatizadas sólidas; riesgoso en equipos que recién comienzan |
---
## 2. Estrategia adoptada: GitFlow
Para este proyecto se utilizará **GitFlow**, ya que permite separar el código estable del código en desarrollo y organizar el trabajo mediante ramas.

La estructura principal es:
- `main`: contiene la versión estable del proyecto.
- `develop`: rama principal de integración de los cambios.
- `feature/<nombre>`: utilizada para desarrollar nuevas funcionalidades.
- `hotfix/<nombre>`: utilizada para corregir errores urgentes.
### Justificación de la elección
las razones por la que trabajamos con esta es debido a que nos permite tener un control estricto sobre el ciclo de vida de nuestro software la principal ventaja es que separamos las ramas para poder avanzar simultáneamente sobre la rama develop sin modificar nada del main esto lo diferencia de github flow que manda cada pull request aprobado directo al main y de trunk based que es todo sobre una misma rama
### Flujo de trabajo
```
feature/<nombre>  →  develop  →  main
hotfix/<nombre>   →  main  (y se propaga de vuelta a develop)
```

Las nuevas funcionalidades se desarrollan en ramas `feature` para luego integrarse en `develop`. Las correcciones urgentes se desarrollan en ramas `hotfix` que nacen de `main`, se integran a `main` y se propagan a `develop` para que la corrección no se pierda. Los cambios que llegan a `main` deben estar previamente revisados y validados mediante Pull Request.
Para reforzar esta estrategia, la **rama por defecto del repositorio es `develop`**, de modo que todo Pull Request nuevo proponga automáticamente `develop` como rama base.

---

## 3. Guía de buenas prácticas
### 3.1 Naming de ramas
```
feature/<descripcion-en-kebab-case>   nueva funcionalidad
hotfix/<descripcion-en-kebab-case>    corrección urgente sobre main
release/<version>                     preparación de una versión
docs/<tema>                           solo documentación
```
Reglas: minúsculas, sin tildes ni `ñ`, sin espacios, máximo unas cuatro palabras y descripción del *qué*, no del *quién*. La rama se elimina una vez integrada.
### 3.2 Mensajes de commit
Se utiliza la convención **Conventional Commits**:
```
<tipo>(<alcance opcional>): <qué cambió, en imperativo>
```
| Tipo | Uso |
|---|---|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de un error |
| `docs` | Documentación |
| `ci` | Pipeline y automatización |
| `test` | Pruebas |
| `refactor` | Cambio interno sin alterar el comportamiento |
| `chore` | Mantenimiento |
Ejemplos utilizados en este repositorio:
```
ci: agregar pipeline de build y test con GitHub Actions
ci: marcar mvnw como ejecutable para el runner de Linux
feat(auth): exponer endpoint publico de health check
```
### 3.3 Estructura de carpetas
```
devops_007d_ols/
├── .github/
│   └── workflows/ci.yml        pipeline de integración continua
├── src/
│   ├── main/java/com/carmeet/ms_auth_user/
│   │   ├── config/             configuración de seguridad y beans
│   │   ├── controller/         endpoints REST
│   │   ├── dto/                objetos de entrada y salida
│   │   ├── exception/          manejo global de errores
│   │   ├── model/              entidades JPA
│   │   ├── repository/         acceso a datos
│   │   ├── security/           JWT y user details
│   │   └── service/            lógica de negocio
│   ├── main/resources/
│   │   ├── application.properties
│   │   └── db/migration/       migraciones Flyway versionadas (V1, V2, …)
│   └── test/java/…             espejo del árbol de main
├── pom.xml
└── README.md
```
Cada capa tiene una responsabilidad única y el árbol de pruebas replica el de código fuente, de modo que la ubicación de un test sea predecible.
### 3.4 Control de versiones
- Las versiones estables de `main` se marcan con **tags anotados** siguiendo versionado semántico (`MAJOR.MINOR.PATCH`):
  ```
  git tag -a v1.0.0 -m "Primera version estable con pipeline CI"
  git push origin v1.0.0
  ```
- Las migraciones de base de datos se versionan con Flyway (`V1__init.sql`, `V2__refresh_token.sql`) y nunca se modifican una vez aplicadas.
- Ningún valor sensible se versiona: los secretos se inyectan como variables de entorno desde GitHub Actions Secrets.
- El archivo `.gitignore` excluye artefactos de compilación (`target/`) y configuración local de IDE.
### 3.5 Estrategia de revisión
- Nada entra a `main` ni a `develop` sin Pull Request.
- Todo Pull Request requiere la aprobación del otro integrante.
- El pipeline de GitHub Actions debe estar en verde antes de integrar.
- Se integra con **Create a merge commit** (no *squash*), para conservar en el historial la trazabilidad de cada rama.
- La rama se elimina en GitHub una vez integrada.
---
## 4. Flujo colaborativo simulado
### Comandos Git utilizados
```bash
# obtener el repositorio y configurar la identidad
git clone https://github.com/pvscalpch/devops_007d_ols.git
git config user.name "..."  &&  git config user.email "..."
# trabajar una funcionalidad
git switch develop
git pull origin develop
git switch -c feature/<nombre>
git add <archivos>
git commit -m "feat(...): ..."
git push -u origin feature/<nombre>

# integrar y sincronizar
git merge origin/main -m "chore: sincronizar develop con main"
git push origin develop

# limpieza y versionado
git push origin --delete feature/<nombre>
git tag -a v1.0.0 -m "..."  &&  git push origin v1.0.0

# inspección de la trazabilidad
git log --all --oneline --graph --decorate
```

### Pull Requests

| # | Título | Rama origen → destino | Autor | Revisor | Estado |
|---|---|---|---|---|---|
| 1 | Feature/ci pipeline | `feature/ci-pipeline` → `main` | cristianbv1256-bit | | Mergeado |
| 2 | feat(auth): exponer endpoint publico de health check | `feature/health-endpoint` → `main` | cristianbv1256-bit | | Mergeado |
| 3 |  documentación completa del repositorio | `docs/readme-completo` → `develop` | | | |
| 4 |  release v1.0.0 | `develop` → `main` | | | |
| 5 |  hotfix del secreto JWT | `hotfix/jwt-secret-env` → `main` | | | |

La trazabilidad completa del historial se encuentra en `docs/trazabilidad.txt`, generado con:
```bash
git log --all --graph --decorate --date=short --pretty=format:"%h %ad %an %s" > docs/trazabilidad.txt
```
---
## 5. Pipeline CI/CD con GitHub Actions
El archivo [`.github/workflows/ci.yml`](.github/workflows/ci.yml) define el pipeline de integración continua del microservicio.
### Cuándo se ejecuta
| Evento | Rama | Propósito |
|---|---|---|
| `push` | `develop` | Validar cada integración a la rama de desarrollo |
| `pull_request` | → `main` | Impedir que llegue a producción código que no compila o cuyas pruebas fallan |
| `workflow_dispatch` | cualquiera | Ejecución manual para probar una rama antes de abrir el Pull Request |
### Qué hace
1. **Levanta un contenedor MySQL 8.0** como servicio del job, con una base `db_auth` desechable. Esto simula el entorno cloud donde se despliega el microservicio, sin depender de ninguna base de datos local.
2. **Descarga el código** del repositorio.
3. **Instala el JDK 17** (Temurin) y cachea las dependencias de Maven para acelerar ejecuciones posteriores.
4. **Compila, aplica las migraciones Flyway y ejecuta las pruebas** con `./mvnw -B -ntp verify`, apuntando la conexión al MySQL del pipeline mediante variables de entorno.
5. **Publica el `.jar`** resultante como artefacto descargable desde la ejecución.
6. **Escribe un resumen** con la rama, el commit y el estado final.

### Rol dentro de CI/CD
> - ¿Qué problema evita este pipeline que antes había que detectar a mano?
  Que llegue a la rama estable codigo que no compila o que sus test fallen
> - ¿Por qué el secreto `JWT_SECRET` se inyecta desde GitHub Secrets y no se escribe en el código?
Porque el repositorio es publico y esa es la firma de los token cualquiera que la lea puede fabricar un token y hacerse pasar por cualquier usuario del sistema
> - ¿Qué pasaría si el pipeline fallara en un Pull Request hacia `main`?
no mergearia en el main debido a que al no compilar el main es la rama por así decirlo estable o funcional para eso se configura una regla de proteccion de rama que exige que el pipeline funcione antes de poder hacer el merge
> - ¿Qué faltaría para convertir esta integración continua en un despliegue continuo (CD)?
para que se pueda desplegar necesita algo que se pueda desplegar en un entorno real como un docker un servidor o un servicio cloud 
### Ejecuciones registradas

| Run | Evento | Rama | Resultado |
|---|---|---|---|
| #2 | `pull_request` | `feature/ci-pipeline` → `main` | ✅ |
| #3 | `push` | `develop` | ✅ |
| #4 | `pull_request` | `feature/health-endpoint` → `main` | ✅ |

---

## 6. Cómo ejecutar el microservicio localmente

Requisitos: JDK 17 o superior y una instancia de MySQL en `localhost:3306`.

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8090`. Endpoint público de verificación:

```
GET http://localhost:8090/api/health
```

---

---

# Evaluación Parcial 2 — Pipeline CI/CD completo

El pipeline está definido en [`.github/workflows/ci.yml`](.github/workflows/ci.yml) y encadena seis etapas. Cada una depende de que la anterior haya terminado correctamente, de modo que nada llega al despliegue sin haber pasado por pruebas, análisis de código y escaneo de seguridad.

```
unit-tests ─┬─ integration-tests ──┐
            └─ code-analysis ──────┴─ docker-build ─ security ─ deploy
   IE2            IE2                    IE1           IE3        IE4
                SonarCloud                            Trivy
```

## 7. Contenerización del microservicio

El [`Dockerfile`](Dockerfile) usa una construcción en dos etapas:

| Etapa | Imagen base | Qué hace | Llega a la imagen final |
|---|---|---|---|
| `build` | `maven:3.9-eclipse-temurin-17` | Resuelve dependencias y compila el `.jar` | No |
| Ejecución | `eclipse-temurin:17-jre-alpine` | Ejecuta la aplicación | Sí |

Decisiones de configuración:

- **Orden de capas.** Se copia `pom.xml` y se resuelven las dependencias *antes* de copiar `src`. Un cambio en el código no vuelve a descargar el repositorio Maven completo.
- **Usuario sin privilegios.** La aplicación corre como `spring`, no como `root`. Se verifica en el log de arranque: `started by spring in /app`.
- **`HEALTHCHECK`.** Consulta `/api/health` cada 30 segundos, con 60 de margen inicial para el arranque de Spring. Docker reporta el contenedor como `healthy` solo cuando la aplicación responde de verdad.
- **Memoria.** Se usa `-XX:MaxRAMPercentage=75.0` en lugar de un `-Xmx` fijo, para que la JVM se ajuste al límite que le asigne el orquestador.
- **Las pruebas no corren dentro del `Dockerfile`**, porque necesitan MySQL. Se ejecutan en sus propios jobs del pipeline.

El archivo [`.dockerignore`](.dockerignore) evita enviar `target/` y `.git` al contexto de construcción.

La imagen se publica en GitHub Container Registry etiquetada con el SHA del commit y con `latest`:

```
ghcr.io/pvscalpch/ms-auth-user:sha-<commit>
```

> ✍️ **PENDIENTE — lo escribe el equipo, sin IA.** Por qué eligieron una construcción en dos etapas y qué ganaron con ella frente a una sola.

## 8. Pruebas automatizadas y política de calidad

### Política de calidad del proyecto

1. Toda clase con lógica de negocio o de seguridad tiene pruebas unitarias que no dependen de infraestructura.
2. Las pruebas unitarias se ejecutan en cada push y en cada Pull Request, antes que cualquier otra etapa.
3. Una prueba en rojo detiene el pipeline: no se construye imagen ni se despliega.
4. La cobertura se mide con JaCoCo y el reporte se publica como artefacto de cada ejecución.
5. Las pruebas que requieren base de datos se ejecutan por separado y se nombran como pruebas de integración.

### Casos críticos cubiertos

| Prueba | Clase | Caso crítico que protege |
|---|---|---|
| `emiteTokenValidoConUsuarioYRol` | `JwtUtilTest` | Un token legítimo conserva identidad y rol |
| `rechazaTokenAdulterado` | `JwtUtilTest` | Un token con la firma alterada no se acepta |
| `rechazaTokenDeOtroEmisor` | `JwtUtilTest` | Un token firmado con otra clave no se acepta |
| `distingueRefreshDeTokenDeAcceso` | `JwtUtilTest` | Un refresh token no sirve para autenticarse |
| `rechazaCadenaQueNoEsUnToken` | `JwtUtilTest` | Una entrada inválida no provoca una excepción no controlada |
| `rechazaRegistroDuplicado` | `AuthServiceTest` | No se puede registrar dos veces el mismo usuario |
| `registraConClaveCifradaYRolPorDefecto` | `AuthServiceTest` | La contraseña nunca se guarda en texto plano |
| `rechazaLoginConCredencialesInvalidas` | `AuthServiceTest` | Un login fallido no emite ningún token |
| `emiteTokensEnLoginCorrecto` | `AuthServiceTest` | Un login válido devuelve los tokens y el rol correctos |
| `promueveUsuarioAAdministrador` | `AuthServiceTest` | El cambio de rol se persiste |
| `fallaAlPromoverUsuarioInexistente` | `AuthServiceTest` | No se opera sobre usuarios que no existen |
| `cargaUsuarioConSuRol` | `CustomUserDetailsServiceTest` | El rol almacenado se traduce a la autoridad correcta |
| `fallaConUsuarioInexistente` | `CustomUserDetailsServiceTest` | Un usuario inexistente produce el error esperado |

Las pruebas unitarias usan **JUnit 5**, **Mockito** y **AssertJ**, y no levantan el contexto de Spring: todas las dependencias se simulan. La prueba de integración `AuthUserApplicationTests` sí levanta el contexto completo y se ejecuta en el job `integration-tests`, que dispone de un MySQL efímero.

## 9. Seguridad y escalabilidad del pipeline

### Seguridad

Se combinan tres herramientas, cada una en una capa distinta:

| Herramienta | Qué revisa | Efecto |
|---|---|---|
| **Dependabot** | Dependencias de Maven, acciones del pipeline e imágenes base | Alerta: abre un Pull Request por cada actualización disponible |
| **SonarCloud** | Código fuente del proyecto | Alerta: `-Dsonar.qualitygate.wait=true` espera el Quality Gate y lo reporta como un check propio del commit |
| **Trivy** | Dependencias del proyecto y sistema operativo de la imagen | **Bloquea**: `exit-code: 1` ante vulnerabilidades `CRITICAL` en la imagen detiene el pipeline |

Los hallazgos de Trivy se publican en formato SARIF y aparecen en la pestaña **Security → Code scanning** del repositorio.

**La división de responsabilidades es deliberada.** Dependabot no puede reprobar un pipeline: no es un paso de workflow, sino una configuración del repositorio que abre Pull Requests. Cumple el rol de alerta. El bloqueo lo aportan SonarCloud y Trivy, que sí se ejecutan como pasos y devuelven un código de salida distinto de cero.

Ningún valor sensible está en el repositorio: `SONAR_TOKEN` y `JWT_SECRET` se inyectan desde GitHub Actions Secrets, y las credenciales de la base de datos desde un archivo `.env` que no se versiona.

### Escalabilidad

| Parámetro | Dónde | Para qué |
|---|---|---|
| `concurrency` con `cancel-in-progress` | Workflow | Cancela ejecuciones obsoletas de la misma rama en lugar de competir por los runners |
| `timeout-minutes` | Cada job | Un cuelgue no consume la cuota de minutos indefinidamente |
| `cache: maven` y `cache-from: type=gha` | Jobs de Java y de Docker | Reutiliza dependencias y capas entre ejecuciones |
| `deploy.resources.limits` | `docker-compose.yml` | Techo de CPU y memoria por servicio |
| `-XX:MaxRAMPercentage=75.0` | `Dockerfile` | La JVM respeta el límite del contenedor en lugar de ver la memoria del anfitrión |

> ✍️ **PENDIENTE — lo escribe el equipo, sin IA.** Qué problema concreto evita este esquema de seguridad y qué debería ocurrir si un análisis falla en un Pull Request hacia `main`.

## 10. Despliegue automático y trazabilidad

El job `deploy` se ejecuta automáticamente con cada push a `develop`, después de que las pruebas, el análisis de código y el escaneo de seguridad hayan pasado.

1. Descarga del registro la imagen **de ese commit**, identificada por su SHA.
2. Levanta el stack completo con `docker compose up -d --no-build --wait`.
3. Consulta `/api/health` hasta veinte veces y verifica que responde `"status":"UP"`. Si no responde, vuelca los logs y falla.
4. Escribe un resumen con commit, autor, imagen desplegada y resultado.
5. Guarda los logs del despliegue como artefacto descargable.

### Cómo se garantiza la trazabilidad

| Eslabón | Identificador | Dónde se consulta |
|---|---|---|
| Commit | SHA | Historial de Git |
| Imagen | `ms-auth-user:sha-<commit>` | Pestaña Packages del repositorio |
| Despliegue | Entorno `staging` | Pestaña Environments |
| Verificación | Respuesta de `/api/health` | Log del job y resumen de la ejecución |

La cadena se recorre en los dos sentidos: desde un commit se sabe qué imagen produjo y dónde quedó corriendo, y desde un contenedor en ejecución se sabe de qué commit salió, porque el tag **nunca es `latest`** sino el SHA exacto.

El entorno cloud simulado es el runner de GitHub Actions: una máquina efímera aprovisionada bajo demanda, donde el stack se levanta, se verifica y se desmonta en cada ejecución.

## 11. Orquestación de contenedores

El archivo [`docker-compose.yml`](docker-compose.yml) define el sistema completo:

| Servicio | Imagen | Función |
|---|---|---|
| `mysql` | `mysql:8.0` | Base de datos, con volumen persistente |
| `app` | `ghcr.io/pvscalpch/ms-auth-user` | Microservicio de autenticación |

Mecanismos que sostienen la estabilidad:

- **Healthcheck en ambos servicios.** `mysqladmin ping` para la base y `curl /api/health` para la aplicación.
- **`depends_on` con `condition: service_healthy`.** La aplicación arranca cuando MySQL *acepta conexiones*, no cuando el contenedor existe. Sin esta condición el arranque falla de forma intermitente, porque MySQL tarda unos 25 segundos en inicializarse la primera vez.
- **Volumen nombrado `mysql-data`.** Los datos sobreviven a `docker compose down` y a cualquier reinicio.
- **`restart: unless-stopped`.** Un contenedor caído se levanta solo.
- **Límites de CPU y memoria** por servicio, coherentes con el ajuste de la JVM.
- **Variables externalizadas** en `.env`, con [`.env.example`](.env.example) versionado como plantilla y el `.env` real excluido del repositorio.

Verificación del arranque ordenado:

```
✔ Container carmeet-mysql   Healthy   17.2s
✔ Container carmeet-auth    Healthy   32.1s
```

MySQL alcanza el estado saludable a los 17 segundos y la aplicación arranca después, a los 32.

> ✍️ **PENDIENTE — lo escribe el equipo, sin IA.** Por qué eligieron Docker Compose como estrategia de orquestación y qué límites tendría frente a un escenario de mayor escala.

---

## 12. Declaración de uso de Inteligencia Artificial
 las herramientas ia que utilizamos fueron Claude mas que nada para corregir errores al momento de hacer el pull request que se fueron para una rama que no era y en la redacción del README 
---
## 8. Conclusiones y reflexiones individuales
### Cristian Bravo

Bueno por mi parte las cosas que aprendí al hacer esta primera evaluación fue a trabajar de una forma distinta como lo había hecho antes debido a que en su momento trabaje con Github Flow donde cada uno tenia una rama temporal feature donde hacia sus avances en sus ramas clonaba la rama de mi compañero y le añadia las cosas que faltaban esto fue un desafío para mi debido a que me costaba entenderlo desde un inicio las cosas que contribuí fue que arregle el error del mvnw que fallaba solo en Linux, el error de que los pull request se iban a la rama equivocada y agregue el /api/health con su regla de seguridad

### Pascal Pacheco

Por mi parte, esta evaluación me sirvió harto para reforzar mis conocimientos sobre Git y GitHub, ya que había varias cosas que no recordaba tan bien o que simplemente no sabía. Esto fue una de las razones por las que tomé este optativo, y creo que hacer el proyecto de forma práctica me ayudó bastante a entender mejor el uso de ramas, Pull Requests y GitFlow.