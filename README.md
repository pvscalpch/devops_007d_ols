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

### Por qué una construcción en dos etapas

Elegimos separar la compilación de la ejecución porque son dos necesidades distintas. Para compilar hace falta Maven, el JDK completo y el repositorio de dependencias descargado; para ejecutar la aplicación no hace falta nada de eso, solo un JRE y el `.jar`.

Con una sola etapa, la imagen que termina corriendo en el servidor llevaría dentro un compilador, un gestor de dependencias y el código fuente completo. Eso tiene dos costos: ocupa varias veces más espacio, y le entrega herramientas a cualquiera que consiga acceso al contenedor. Separando las etapas, la imagen final queda en 406 MB y contiene únicamente lo necesario para que el servicio funcione.

El orden de las instrucciones también fue deliberado. Copiamos el `pom.xml` y resolvemos las dependencias antes de copiar `src`, de modo que Docker guarde esa capa y la reutilice mientras el `pom` no cambie. La diferencia se nota: la primera construcción tardó varios minutos descargando el repositorio de Maven entero, y las siguientes, con solo cambios en el código, fueron mucho más rápidas.

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

**La división de responsabilidades es deliberada.** Dependabot no puede reprobar un pipeline: no es un paso de workflow, sino una configuración del repositorio que abre Pull Requests. Cumple el rol de alerta. El bloqueo lo aporta Trivy sobre la imagen, que se ejecuta como paso y devuelve un código de salida distinto de cero.

### El bloqueo en funcionamiento

Durante la construcción de este pipeline el mecanismo se activó dos veces de forma real, sin provocarlo:

1. **Dependabot propuso subir `jjwt` de 0.11.5 a 0.13.0.** El pipeline detectó que esa versión cambia la API y rompe la compilación, y el Pull Request quedó en rojo. La actualización no pudo integrarse.
2. **Trivy detuvo el pipeline por 10 vulnerabilidades `CRITICAL`** en la imagen, antes de permitir el despliegue.

### Riesgos aceptados

El escaneo del sistema operativo de la imagen (`alpine 3.24.3`) reporta **0 vulnerabilidades**, tras actualizar la imagen base del runtime a `eclipse-temurin:25-jre-alpine` siguiendo una propuesta de Dependabot.

Las 10 vulnerabilidades `CRITICAL` restantes están en dependencias transitivas que arrastra `spring-boot-starter-parent 4.0.6`:

| Librería afectada | Vulnerabilidades | Corregida en |
|---|---|---|
| `org.apache.tomcat.embed:tomcat-embed-core` 11.0.21 | 6 | 11.0.22 / 11.0.25 |
| `org.bouncycastle:bcprov-jdk18on` 1.81 | 2 | 1.84 / 1.85 |
| `org.springframework:spring-webmvc` 7.0.7 | 2 | 7.0.9 |

Las tres se corrigen subiendo el parent de Spring Boot, que es lo que propone un Pull Request abierto por Dependabot. Esa actualización implica un cambio de versión menor del framework y queda pendiente de validación completa.

Mientras tanto, los identificadores concretos están declarados en [`.trivyignore`](.trivyignore) con su justificación y la condición para retirarlos. **Solo se silencian esas vulnerabilidades específicas: cualquier `CRITICAL` nueva sigue deteniendo el pipeline.**

Ningún valor sensible está en el repositorio: `SONAR_TOKEN` y `JWT_SECRET` se inyectan desde GitHub Actions Secrets, y las credenciales de la base de datos desde un archivo `.env` que no se versiona.

### Escalabilidad

| Parámetro | Dónde | Para qué |
|---|---|---|
| `concurrency` con `cancel-in-progress` | Workflow | Cancela ejecuciones obsoletas de la misma rama en lugar de competir por los runners |
| `timeout-minutes` | Cada job | Un cuelgue no consume la cuota de minutos indefinidamente |
| `cache: maven` y `cache-from: type=gha` | Jobs de Java y de Docker | Reutiliza dependencias y capas entre ejecuciones |
| `deploy.resources.limits` | `docker-compose.yml` | Techo de CPU y memoria por servicio |
| `-XX:MaxRAMPercentage=75.0` | `Dockerfile` | La JVM respeta el límite del contenedor en lugar de ver la memoria del anfitrión |

### Qué problema evita este esquema

Evita que una vulnerabilidad conocida llegue a la rama estable sin que nadie se entere. Antes, la única forma de detectarla era que alguien se acordara de revisar las dependencias a mano, y eso no ocurre cuando hay plazos encima.

Los dos casos descritos arriba muestran los dos modos de fallo que cubre. El de `jjwt` es una actualización que parece inofensiva y rompe la compilación: sin pipeline se habría integrado y el error habría aparecido más tarde, lejos de su causa. El de las vulnerabilidades críticas es lo contrario: nada estaba roto, el proyecto compilaba y las pruebas pasaban, pero la imagen cargaba diez fallos de seguridad conocidos. Ningún desarrollador los habría visto leyendo el código.

Si un análisis falla en un Pull Request hacia `main`, la integración no debe permitirse. El check queda en rojo y el revisor lo ve antes de aprobar. Para que eso no dependa del criterio de quien revisa, corresponde configurar una regla de protección de rama que exija el check en verde y deshabilite el botón de merge mientras no se cumpla.

También decidimos no silenciar los hallazgos para que el pipeline pasara. Cuando una vulnerabilidad no se puede corregir de inmediato, se declara en [`.trivyignore`](.trivyignore) con su identificador, su justificación y la condición para retirarla. Un escáner que se ignora deja de servir; uno al que se le declara explícitamente qué se acepta sigue vigilando todo lo demás.

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

### Por qué Docker Compose

Elegimos Docker Compose porque el sistema son tres contenedores que viven en la misma máquina y necesitan arrancar en un orden determinado. Para ese tamaño, Compose resuelve el problema completo con un solo archivo versionado junto al código, y se ejecuta igual en la máquina de cada integrante y en el runner del pipeline. Kubernetes habría resuelto lo mismo a costa de un clúster, varios manifiestos y una curva de aprendizaje que no aportaba nada a un sistema de este tamaño.

El motivo concreto por el que nos hizo falta orquestación, y no simplemente levantar los contenedores por separado, lo descubrimos probando. Al arrancar la aplicación junto a MySQL con comandos sueltos, el microservicio falló con `Connection refused`: había arrancado seis segundos antes de que MySQL terminara de inicializarse y aceptara conexiones. El contenedor de la base figuraba como `Up` desde el primer instante, pero eso no significa que el servicio esté disponible. Esa diferencia es lo que resuelve `depends_on` con `condition: service_healthy`, apoyado en los healthcheck de cada servicio.

**Sus límites.** Compose funciona sobre un solo anfitrión: no reparte contenedores entre varias máquinas, no los reprograma si el nodo cae, y el escalado es manual mediante `--scale`. En un escenario de mayor envergadura —varios nodos, escalado automático según la carga, actualizaciones sin corte de servicio— haría falta un orquestador como Kubernetes, con sus réplicas, sondas de disponibilidad y escalado automático. Para este proyecto esa complejidad no se justifica.

---

## 12. Declaración de uso de Inteligencia Artificial

**Herramienta utilizada:** Claude (Anthropic).

**Evaluación Parcial 1.** Corrección de errores al abrir Pull Requests que se dirigían a una rama incorrecta, y apoyo en la redacción del README.

**Evaluación Parcial 2.**

| Uso | Detalle |
|---|---|
| Código de automatización | Redacción inicial del `Dockerfile`, el `docker-compose.yml`, el `.github/workflows/ci.yml` y el `.github/dependabot.yml` |
| Código de pruebas | Redacción inicial de `JwtUtilTest`, `AuthServiceTest` y `CustomUserDetailsServiceTest` |
| Depuración | Diagnóstico de los fallos del pipeline: el `mvnw` sin permiso de ejecución, la prueba de integración ejecutándose sin base de datos, el bloqueo de Maven Central al escanear, y la configuración de SonarCloud |
| Documentación | Estructura y redacción de este README |
| Referencias | Comparación de modelos de ramificación y de herramientas de análisis |

**Validación realizada por el equipo.** Todo el contenido generado fue revisado y ejecutado antes de integrarse. El `Dockerfile` se probó en local levantando el contenedor contra una base de datos y comprobando el endpoint de salud. Las pruebas se ejecutaron hasta verificar que pasaban las trece. El pipeline se corrigió de forma iterativa hasta obtener los seis jobs en verde, incluido el despliegue verificado.

Las reflexiones individuales de la sección 13 fueron redactadas por cada integrante sin apoyo de inteligencia artificial.

Referencia de citación: <https://bibliotecas.duoc.cl/ia>

---

## 13. Conclusiones y reflexiones individuales

### Cristian Bravo

Bueno por mi parte las cosas que aprendí al hacer esta primera evaluación fue a trabajar de una forma distinta como lo había hecho antes debido a que en su momento trabaje con Github Flow donde cada uno tenia una rama temporal feature donde hacia sus avances en sus ramas clonaba la rama de mi compañero y le añadia las cosas que faltaban esto fue un desafío para mi debido a que me costaba entenderlo desde un inicio las cosas que contribuí fue que arregle el error del mvnw que fallaba solo en Linux, el error de que los pull request se iban a la rama equivocada y agregue el /api/health con su regla de seguridad

> ✍️ **Agregar lo aprendido en la EP2.** Material propio según el historial: contenerización del microservicio, construcción del pipeline de seis etapas, pruebas unitarias con Mockito, configuración de SonarCloud y Trivy, y el despliegue automático verificado.

### Pascal Pacheco

> ✍️ **PENDIENTE — obligatorio, sin IA.** Material propio según el historial: la integración del `docker-compose.yml` a la rama principal, la incorporación del proxy nginx con escalado de réplicas, y las pruebas adicionales de controladores y manejo de excepciones.

Por mi parte, esta evaluación me sirvió harto para reforzar mis conocimientos sobre Git y GitHub, ya que había varias cosas que no recordaba tan bien o que simplemente no sabía. Esto fue una de las razones por las que tomé este optativo, y creo que hacer el proyecto de forma práctica me ayudó bastante a entender mejor el uso de ramas, Pull Requests y GitFlow.