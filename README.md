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

## 12. Orquestación en Kubernetes

**Alcance.** Los manifiestos de [`k8s/`](k8s/) e [`istio/`](istio/) describen el despliegue del microservicio en un clúster de Kubernetes con Istio. **No se aplicaron contra un clúster real:** los laboratorios de AWS disponibles para el curso no permiten crear clústeres de Kubernetes, y hacerlo en una cuenta personal tiene costo. Lo que se entrega son los manifiestos y el razonamiento detrás de cada recurso.

### Los recursos y qué resuelve cada uno

| Recurso | Problema que resuelve |
|---|---|
| `Namespace` | Aísla el proyecto. Las cargas de trabajo de otros namespaces no ven estos recursos. La etiqueta `istio-injection: enabled` es la que hace que Istio opere aquí: sin ella, las políticas de la carpeta `istio/` no tendrían efecto |
| `ServiceAccount` | Da al pod una identidad con nombre. Es a lo que apuntan las políticas de Istio, en lugar de a una dirección IP que cambia en cada reinicio |
| `Secret` | Saca las credenciales del manifiesto del Deployment. El real no se versiona: el campo `data` es base64, que es codificación y no cifrado |
| `Deployment` | Mantiene N réplicas vivas y las reemplaza si caen. El `selector` es el vínculo con sus pods; si no coincidiera con las etiquetas del template, el Deployment no reconocería a los suyos |
| `Service` | IP virtual y nombre DNS estables delante de réplicas que van y vienen. `mysql.ms-auth-user.svc.cluster.local` no cambia aunque el pod se reprograme |
| `Gateway` | Punto de entrada desde Internet. En EKS se materializa como un balanceador de carga de AWS |
| `HTTPRoute` | Reglas sobre ese punto de entrada: qué ruta va a qué Service, y con qué peso |

### Las tres sondas

Docker Compose tiene una sola noción de salud. Kubernetes separa tres preguntas distintas y actúa diferente en cada una, lo que importa porque confundirlas produce fallos difíciles de diagnosticar:

| Sonda | Pregunta | Si falla |
|---|---|---|
| `startupProbe` | ¿Terminó de arrancar? | No pasa nada todavía. Mientras no pase, las otras dos no se evalúan, así que un arranque lento de la JVM no se confunde con un fallo ni provoca un reinicio en bucle |
| `readinessProbe` | ¿Puede recibir tráfico ahora? | El pod sale de los endpoints del Service, pero **no se reinicia**. Es lo que corresponde si la base de datos deja de responder: el pod sigue vivo y vuelve solo cuando la conexión se recupera |
| `livenessProbe` | ¿Sigue vivo? | Kubernetes **reinicia** el contenedor. Cubre el caso del proceso colgado, que no termina pero ya no responde |

El detalle de cada recurso, los comandos `kubectl` en orden y una tabla de equivalencias completa con el `docker-compose.yml` están en [`k8s/README.md`](k8s/README.md).

### Istio

Istio es una **malla de servicios**: inyecta un proxy Envoy junto a cada contenedor, y todo el tráfico de entrada y salida del pod pasa por ahí. De eso salen tres capacidades que la aplicación no tiene que programar:

- **Observabilidad sin instrumentar el código.** El proxy ve cada petición y produce métricas de latencia, volumen y errores por par origen-destino.
- **Control de acceso por identidad.** [`istio/01-authorization-policy.yaml`](istio/01-authorization-policy.yaml) restringe el puerto 3306 de MySQL a la identidad del microservicio de autenticación. Por defecto, dentro de un clúster cualquier pod puede abrir una conexión a cualquier otro; esta política cierra esa red plana.
- **Reparto de tráfico por peso.** [`istio/02-canary.yaml`](istio/02-canary.yaml) despliega una `v2` en paralelo y le envía el 10% del tráfico. Kubernetes por sí solo no puede hacerlo: su Service balancea a partes iguales entre todos los pods que coincidan con el selector, sin noción de peso.

---

## 13. Estrategia de monitoreo y observabilidad

Monitorear y observar no son lo mismo. **Monitorear** es vigilar un conjunto de indicadores decididos de antemano: si la CPU pasa del 80%, avisar. **Observabilidad** es poder responder preguntas que nadie anticipó, a partir de lo que el sistema ya emite, sin tener que desplegar una versión nueva para averiguarlo. Lo primero sirve para saber *que* algo anda mal; lo segundo, para averiguar *por qué*.

### Los tres pilares

| Pilar | Qué pregunta responde | De dónde sale | Dónde se consulta |
|---|---|---|---|
| **Logs** | ¿Qué pasó exactamente, y con qué detalle? | La aplicación escribe a `stdout` | `kubectl logs`, y centralizado en CloudWatch Logs |
| **Métricas** | ¿Cuánto, con qué frecuencia, desde cuándo? | Micrometer en la aplicación, el proxy Envoy de Istio, y el kubelet | Prometheus y CloudWatch Container Insights |
| **Trazas** | ¿Por dónde pasó una petición y dónde se demoró? | El proxy Envoy, sin tocar el código | Kiali |

Ninguno sustituye a los otros. Una métrica dice que el 5% de las peticiones devuelve error; el log dice cuál fue la excepción; la traza dice en qué servicio de la cadena se originó.

### Métricas: qué se obtiene y de dónde

**1. De la aplicación**, vía Spring Boot Actuator y Micrometer. El `pom.xml` incorpora `micrometer-registry-prometheus`, que publica las métricas en `/actuator/prometheus` en el formato que Prometheus sabe leer. Las anotaciones `prometheus.io/scrape` del Deployment son las que hacen que Prometheus descubra el pod y lo raspe.

| Métrica | Para qué sirve |
|---|---|
| `http_server_requests_seconds_count` | Peticiones por endpoint, método y código de respuesta. Separa el tráfico de `/auth/login` del de `/auth/register` |
| `http_server_requests_seconds_sum` y `_max` | Latencia por endpoint. El promedio engaña; el máximo muestra la peor experiencia real de un usuario |
| `hikaricp_connections_active` y `_pending` | Pool de conexiones a MySQL. Si `pending` crece, la base de datos es el cuello de botella y no la aplicación |
| `jvm_memory_used_bytes` | Memoria del heap. Si sube y no baja tras una recolección, hay una fuga |
| `jvm_gc_pause_seconds` | Pausas del recolector de basura. Explican picos de latencia que no se ven en el código |
| `logback_events_total` con `level="error"` | Errores registrados, como métrica. Permite alertar sobre la tasa de errores sin leer un solo log |
| `process_cpu_usage` | Uso de CPU del proceso |

**2. Del proxy Envoy de Istio**, sin tocar una línea de código Java. Istio deposita estas métricas en el Prometheus que se instala junto con él:

| Métrica | Para qué sirve |
|---|---|
| `istio_requests_total` | Peticiones por origen, destino y código. Es lo que dibuja el grafo de Kiali |
| `istio_request_duration_milliseconds` | Latencia en percentiles (p50, p90, p99) entre cada par de servicios |
| `istio_tcp_connections_opened_total` | Conexiones hacia MySQL. Es la métrica que se desploma al aplicar la política `DENY` |
| `response_flags` | Por qué falló una petición: rechazo de política, circuito abierto, tiempo agotado |

La diferencia con el grupo anterior: estas métricas son **por par de servicios**, así que responden "¿quién está llamando a quién y cómo le va?", que la aplicación por sí sola no puede saber.

**3. Del clúster**, vía el complemento *Amazon CloudWatch Observability*, que habilita Container Insights:

| Métrica | Para qué sirve |
|---|---|
| `pod_cpu_utilization` y `pod_memory_utilization` | Consumo real frente a los `limits` declarados. Si el uso roza el límite, el contenedor será estrangulado o terminado por falta de memoria |
| `pod_number_of_container_restarts` | Reinicios. Un número que sube solo casi siempre significa un `livenessProbe` fallando o un proceso que muere |
| `node_cpu_utilization` | Capacidad del nodo, para saber si hay espacio para más réplicas |

### Logs: qué deja el contenedor

La aplicación escribe a la salida estándar y no a un archivo, que es lo correcto en un contenedor: el motor de contenedores la captura y el log deja de ser problema de la aplicación. Lo que queda registrado:

- El arranque de Spring Boot: puerto, perfil activo, tiempo de inicio.
- Flyway: qué migraciones aplicó y cuáles ya estaban, con su número de versión.
- Cada autenticación: `POST /auth/login` y `POST /auth/register` con el nombre de usuario. **Se registra el nombre de usuario, nunca la contraseña ni el token emitido.** Un log es un artefacto que se copia, se reenvía y se conserva meses; una credencial dentro de él es una filtración con fecha diferida.
- Los intentos rechazados por Spring Security.
- Los errores de conexión a la base de datos, con su excepción completa.
- Cualquier excepción no controlada, con su traza de pila.

Se consultan con `kubectl logs -n ms-auth-user deployment/ms-auth-user -f`, pero eso tiene un límite: **el log vive y muere con el pod.** Si el pod se reprograma o se reinicia, el historial anterior desaparece justo cuando hace falta investigar. Esa es la razón de centralizarlos.

### Cómo llegan los logs y las métricas a CloudWatch

Dos complementos del clúster, cada uno para un pilar distinto:

| Complemento | Qué hace | Requisito |
|---|---|---|
| **Amazon CloudWatch Observability** | Habilita Container Insights: recoge métricas de CPU, memoria y reinicios de cada contenedor y nodo | La política `CloudWatchAgentServerPolicy` en el rol del nodo |
| **Fluent Bit** | Se despliega como DaemonSet, un pod por nodo. Lee los archivos de log de los contenedores del nodo y los reenvía a CloudWatch Logs | El mismo rol con permiso de escritura en CloudWatch Logs |

Fluent Bit es un DaemonSet y no un Deployment por un motivo concreto: tiene que haber exactamente una copia **por nodo**, porque lee del disco de ese nodo. Un Deployment con tres réplicas podría poner las tres en la misma máquina y dejar los otros nodos sin recolectar.

Centralizar aporta tres cosas que `kubectl logs` no puede dar: los logs **sobreviven al pod**; se pueden buscar **a través de todas las réplicas a la vez**, en lugar de pod por pod; y se conservan con una política de retención.

Una vez en CloudWatch Logs, la consulta se hace con Logs Insights:

```
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc
| limit 50
```

```
fields @timestamp, @message
| filter @message like /Connection/
| stats count() by bin(5m)
```

La segunda muestra cómo un log se convierte en una métrica: contar errores de conexión agrupados en ventanas de cinco minutos convierte una lista de texto en una curva sobre la que se puede alertar.

### Trazas

Kiali lee las métricas que Istio deposita en Prometheus y dibuja el grafo de servicios: qué habla con qué, con cuánta latencia, qué porcentaje de errores, y un candado en cada enlace que va cifrado con mTLS. En un sistema de dos componentes aporta poco; en uno de decenas de microservicios es la diferencia entre encontrar el cuello de botella y adivinarlo.

### Alertas propuestas

Observar sin alertar solo sirve si alguien está mirando la pantalla. Los umbrales que corresponden a este servicio:

| Condición | Umbral | Qué significaría |
|---|---|---|
| Tasa de respuestas 5xx | Más de 5% durante 5 min | La aplicación está fallando, no el usuario |
| Latencia p99 de `/auth/login` | Más de 2 s durante 5 min | Degradación perceptible; probablemente la base de datos |
| `hikaricp_connections_pending` | Mayor que 0 sostenido | El pool se agotó: las peticiones esperan una conexión libre |
| Reinicios de contenedor | Más de 3 en 15 min | Un `livenessProbe` fallando en bucle |
| Memoria del pod | Sobre el 90% del límite | Terminación por falta de memoria inminente |
| Réplicas listas | Menos que las deseadas durante 10 min | El despliegue no converge |

### Qué se gana en conjunto

Un ejemplo recorriendo los tres pilares. Si los usuarios reportan lentitud al iniciar sesión: la **métrica** `istio_request_duration_milliseconds` muestra que la latencia está en el salto hacia MySQL y no en la aplicación; `hikaricp_connections_pending` confirma que el pool está agotado; el **log** muestra las esperas y los tiempos agotados con su marca de tiempo; la **traza** en Kiali confirma que ninguna otra ruta está afectada. Con un solo pilar, cada uno de esos pasos sería una suposición.

---

## 14. Política de cumplimiento

Cinco controles automáticos, cada uno cubriendo una superficie distinta. Ninguno reemplaza a los otros: un escáner de dependencias no ve una contraseña escrita a mano, y un detector de secretos no ve una vulnerabilidad en una librería.

| Control | Qué vigila | Bloquea |
|---|---|---|
| **SonarCloud** | Calidad y seguridad del código propio: bugs, code smells, cobertura. Quality Gate bloqueante con `-Dsonar.qualitygate.wait=true` | Sí |
| **GitLeaks** | Credenciales filtradas en el código, sobre **toda la historia** del repositorio | Sí |
| **Trivy (imagen)** | Vulnerabilidades del sistema operativo base y de la aplicación empaquetada | Sí, ante severidad `CRITICAL` |
| **Trivy (dependencias)** | Librerías vulnerables declaradas en el `pom.xml` | No, informativo |
| **Dependabot** | Dependencias desactualizadas, con Pull Request automático | No, propone |

### GitLeaks

Lo que lo distingue de una revisión a ojo es el alcance: **escanea la historia completa, no el último commit.** En Git, un secreto borrado sigue estando en el commit que lo introdujo. Aunque se corrija en el commit siguiente y pasen cien commits más, cualquiera que clone el repositorio puede hacer `checkout` de aquel commit y leer el valor. Un `git rm` no borra nada; solo deja de mostrarlo.

El job es bloqueante y `docker-build` depende de él, de modo que un hallazgo detiene el pipeline antes de construir y publicar la imagen.

Los falsos positivos se declaran uno por uno en [`.gitleaks.toml`](.gitleaks.toml), con el mismo criterio que se aplicó a [`.trivyignore`](.trivyignore): se parte del conjunto completo de reglas por defecto, no se desactiva ninguna, y solo se agregan excepciones nombradas y justificadas. Las que hay son claves de desarrollo local, valores de respaldo de los jobs del pipeline y la clave literal que exige HS256 en las pruebas unitarias. Un escáner cuyos falsos positivos se silencian en bloque deja de servir; uno al que se le declara exactamente qué se acepta sigue vigilando todo lo demás.

Por la misma razón, el Secret de Kubernetes no se versiona. [`k8s/01-secrets.example.yaml`](k8s/01-secrets.example.yaml) es una plantilla con valores de relleno; el real se crea con `kubectl create secret` contra el clúster, sin que los valores pasen nunca por Git.

### Protección de la rama main

Los controles anteriores solo sirven si no se pueden esquivar. Un ruleset sobre la rama por defecto impide cuatro cosas:

| Regla | Qué impide |
|---|---|
| **Block force pushes** | Reescribir la historia de `main`. Un `push --force` puede borrar commits de otra persona sin dejar rastro |
| **Restrict deletions** | Borrar la rama principal |
| **Require a pull request before merging** | Empujar directo a `main` sin revisión |
| **Require status checks to pass** | Integrar código con el pipeline en rojo |

La lista de excepciones del ruleset se deja **vacía**. Agregar ahí al administrador del repositorio permitiría al dueño saltarse la regla, y una protección que el dueño puede ignorar no protege de lo más probable, que es el error propio bajo presión de tiempo.

Un detalle sobre los status checks exigidos: se exigen los cinco que sí se ejecutan en un Pull Request, es decir la auditoría de secretos, las pruebas unitarias, las pruebas de integración, la construcción de la imagen y el análisis de seguridad. **No** se exigen el análisis de SonarCloud ni el despliegue, porque ambos están configurados para correr solo en `push` y nunca reportarían en un Pull Request: exigirlos dejaría el merge bloqueado para siempre esperando un check que no va a llegar.

---

## 15. Interrupción del pipeline ante un fallo

El pipeline se detiene en cualquiera de estas puertas, y cada etapa posterior depende explícitamente del resultado de la anterior:

| Puerta | Qué comprueba | Qué pasa si falla |
|---|---|---|
| Pruebas unitarias | 13 pruebas con Mockito, sin base de datos | Se detiene. No se construye nada |
| Auditoría de secretos | GitLeaks sobre la historia completa | Se detiene antes de construir la imagen |
| Pruebas de integración | Arranque real contra MySQL con migraciones Flyway | Se detiene. No se construye la imagen |
| Análisis de código | Quality Gate de SonarCloud, con espera bloqueante | Se detiene |
| Análisis de seguridad | Trivy sobre la imagen, severidad `CRITICAL` | Se detiene. No se despliega |
| Despliegue | Verificación del endpoint de salud tras levantar el stack | Se marca en rojo y se publican los logs |

La condición de cada etapa comprueba de forma explícita el resultado de la anterior en lugar de heredarlo. Esa regla existe por un problema concreto que apareció durante la EP2: un job omitido en un Pull Request propagaba la omisión por toda la cadena, y el escaneo de seguridad terminaba sin ejecutarse.

### Demostración

Para verificar que la interrupción funciona, basta con romper una aserción de una prueba unitaria, por ejemplo afirmar en `JwtUtilTest` que un token adulterado es válido, y empujar el cambio. El job de pruebas unitarias falla, y los jobs siguientes quedan sin ejecutar: no se construye la imagen, no se publica en el registro y no se despliega nada. Con el ruleset activo, el botón de merge del Pull Request queda además deshabilitado.

### Casos reales durante el desarrollo

El mecanismo ya detuvo integraciones defectuosas de verdad, no solo en una prueba preparada:

- Una actualización automática de `jjwt` propuesta por Dependabot **rompía la compilación**. Sin pipeline se habría integrado y el error habría aparecido más tarde, lejos de su causa.
- El escaneo de Trivy encontró **diez vulnerabilidades críticas** en la imagen. Nada estaba roto: el proyecto compilaba y las pruebas pasaban. Ningún desarrollador las habría visto leyendo el código.
- Una prueba de integración se ejecutaba en un job sin base de datos y fallaba. El pipeline lo detectó y obligó a separar las pruebas unitarias de las de integración con una etiqueta de JUnit.

---

## 16. Declaración de uso de Inteligencia Artificial

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

**Validación realizada en la EP2.** Todo el contenido generado fue revisado y ejecutado antes de integrarse. El `Dockerfile` se probó en local levantando el contenedor contra una base de datos y comprobando el endpoint de salud. Las pruebas se ejecutaron hasta verificar que pasaban las trece. El pipeline se corrigió de forma iterativa hasta obtener los seis jobs en verde, incluido el despliegue verificado.

**Evaluación Parcial 3.**

| Uso | Detalle |
|---|---|
| Manifiestos | Redacción inicial de los archivos de `k8s/` e `istio/`, adaptados al microservicio a partir de los ejemplos mostrados en clase |
| Configuración | El job de GitLeaks en el pipeline, el archivo `.gitleaks.toml` y la incorporación del registro de Prometheus a Actuator |
| Documentación | Estructura y redacción de las secciones 12 a 15, y de los archivos `k8s/README.md` e `istio/README.md` |
| Referencias | Nombres y significado de las métricas de Micrometer, Istio y Container Insights |

**Validación realizada en la EP3.** Los catorce recursos de los manifiestos se verificaron sintácticamente antes de integrarse. La incorporación de `micrometer-registry-prometheus` se compiló y se ejecutaron las trece pruebas unitarias en local antes de empujar el cambio. El job de GitLeaks se comprobó en verde sobre el historial completo del repositorio en la ejecución del Pull Request.

Las reflexiones individuales de la sección 17 fueron redactadas por cada integrante sin apoyo de inteligencia artificial.

Referencia de citación: <https://bibliotecas.duoc.cl/ia>

---

## 17. Conclusiones y reflexiones individuales

### Cristian Bravo

Bueno por mi parte las cosas que aprendí al hacer esta primera evaluación fue a trabajar de una forma distinta como lo había hecho antes debido a que en su momento trabaje con Github Flow donde cada uno tenia una rama temporal feature donde hacia sus avances en sus ramas clonaba la rama de mi compañero y le añadia las cosas que faltaban esto fue un desafío para mi debido a que me costaba entenderlo desde un inicio las cosas que contribuí fue que arregle el error del mvnw que fallaba solo en Linux, el error de que los pull request se iban a la rama equivocada y agregue el /api/health con su regla de seguridad

> ✍️ **Agregar lo aprendido en la EP2.** Material propio según el historial: contenerización del microservicio, construcción del pipeline de seis etapas, pruebas unitarias con Mockito, configuración de SonarCloud y Trivy, y el despliegue automático verificado.

### Pascal Pacheco

Por mi parte, esta evaluación me sirvió harto para reforzar mis conocimientos sobre Git y GitHub, ya que había varias cosas que no recordaba tan bien o que simplemente no sabía. Esto fue una de las razones por las que tomé este optativo, y creo que hacer el proyecto de forma práctica me ayudó bastante a entender mejor el uso de ramas, Pull Requests y GitFlow.
