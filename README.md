# ms-auth-user — DevOps 007D

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
| 3 | *(pendiente)* documentación completa del repositorio | `docs/readme-completo` → `develop` | | | |
| 4 | *(pendiente)* release v1.0.0 | `develop` → `main` | | | |
| 5 | *(pendiente)* hotfix del secreto JWT | `hotfix/jwt-secret-env` → `main` | | | |


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
>   Que llegue a la rama estable codigo que no compila o que sus test fallen
> - ¿Por qué el secreto `JWT_SECRET` se inyecta desde GitHub Secrets y no se escribe en el código?
Porque el repositorio es publico y esa es la firma de los token cualquiera que la lea puede fabricar un token y hacerse pasar por cualquier usuario del sistema
> - ¿Qué pasaría si el pipeline fallara en un Pull Request hacia `main`?
no mergeariea en el main debido a que al no compilar el main es la rama por asi decirlo estable o funcional para eso se configura una regla de proteccion de rama que exige que el pipeline funcione antes de poder hacer el merge
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

## 7. Declaración de uso de Inteligencia Artificial
 las herramientas ia que utilizamos fueron Claude mas que nada para corregir errores al momento de hacer el pull request que se fueron para una rama que no era y en la redaccion del README 
> Indicar qué herramientas de IA se utilizaron y en qué partes concretas del trabajo
> (por ejemplo: redacción de documentación, generación del archivo de workflow,
> diagnóstico de errores del pipeline), y qué revisó y validó el equipo.
> Citar según <https://bibliotecas.duoc.cl/ia>.

---

## 8. Conclusiones y reflexiones individuales

> ✍️ **PENDIENTE — obligatorio, sin IA.**
> Una reflexión personal por cada integrante, explicando su aprendizaje
> y su contribución al proyecto.

### Cristian Bravo

### Pascal Pacheco
