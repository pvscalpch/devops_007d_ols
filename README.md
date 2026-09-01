 DevOps 007D 

||Integrantes: Cristian Bravo y Pascal Pacheco 

||Descripción

Este repositorio contiene el microservicio `ms-auth-user` del proyecto CarMeet.

El microservicio se utiliza como base para implementar un flujo de trabajo DevOps utilizando Git y GitHub.

||Estrategia de ramificación

Para este proyecto se utilizará GitFlow, debido a que permite separar el código estable del código que se encuentra en desarrollo y organizar el trabajo mediante ramas.

La estructura principal será:

- `main`: contiene la versión estable del proyecto.
- `develop`: rama principal de integración de los cambios.
- `feature/<nombre>`: utilizada para desarrollar nuevas funcionalidades.
- `hotfix/<nombre>`: utilizada para corregir errores urgentes.

||Flujo de trabajo

El flujo de trabajo definido será:

`feature/<nombre>` → `develop` → `main`

Las nuevas funcionalidades se desarrollarán en ramas `feature`, para luego integrarse en `develop`.

Las correcciones urgentes se desarrollarán mediante ramas `hotfix`.

Los cambios que lleguen a `main` deberán encontrarse previamente revisados y validados.

||Microservicio

El microservicio utilizado como base es:

`ms-auth-user`

Tecnologías principales:

- Java
- Spring Boot
- Maven
- Git
- GitHub

||Control de versiones

Se utilizará Git para mantener la trazabilidad de los cambios realizados sobre el código fuente.

Los cambios serán registrados mediante commits descriptivos y las integraciones entre ramas se realizarán mediante Pull Requests.

||Trabajo colaborativo

El repositorio permitirá que los integrantes trabajen de forma independiente mediante ramas de funcionalidades y posteriormente integren sus cambios en `develop`.

La revisión de los cambios se realizará mediante Pull Requests antes de incorporarlos a las ramas principales.

