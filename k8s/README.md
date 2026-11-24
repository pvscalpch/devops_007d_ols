# Manifiestos de Kubernetes

Despliegue del microservicio `ms-auth-user` y su base de datos en un clúster de Kubernetes con Istio instalado.

## Alcance

Estos manifiestos son la descripción declarativa del despliegue. **No se aplicaron contra un clúster real**: los laboratorios de AWS disponibles para el curso no permiten crear clústeres de Kubernetes, y hacerlo en una cuenta personal tiene costo. El trabajo consiste en los manifiestos y en el entendimiento de lo que cada uno hace.

## Orden de aplicación

El orden importa, y el prefijo numérico de cada archivo lo refleja. Cada recurso depende de que exista el anterior.

```bash
# 1. El namespace, porque todo lo demás vive dentro de él
kubectl apply -f k8s/00-namespace.yaml

# 2. Las credenciales. NO se aplica el archivo .example: el Secret real
#    se crea de forma imperativa para que los valores no pasen por Git
kubectl create secret generic ms-auth-user-secrets \
  --namespace ms-auth-user \
  --from-literal=mysql-root-password='<clave generada>' \
  --from-literal=jwt-secret='<clave de al menos 32 caracteres>'

# 3. La base de datos, antes de la aplicación: Flyway corre las
#    migraciones contra ella durante el arranque
kubectl apply -f k8s/02-mysql.yaml
kubectl wait --for=condition=available --timeout=180s \
  deployment/mysql -n ms-auth-user

# 4. El microservicio
kubectl apply -f k8s/03-ms-auth-user.yaml

# 5. La exposición hacia el exterior
kubectl apply -f k8s/04-httproute.yaml
```

## Verificación

```bash
# Estado de los pods. La columna READY distingue "corriendo" de
# "aceptando tráfico": 0/2 significa que el readinessProbe aún no pasa
kubectl get pods -n ms-auth-user

# Logs de la aplicación (el pilar de observabilidad que no necesita
# ninguna herramienta extra)
kubectl logs -n ms-auth-user deployment/ms-auth-user -f

# Por qué un pod no arranca: la sección Events explica si falló la
# descarga de la imagen, si falta el Secret o si el probe no pasa
kubectl describe pod -n ms-auth-user -l app=ms-auth-user

# Punto de entrada asignado por el balanceador de AWS
kubectl get gateway shared-gateway -n default
```

## Qué recurso hace qué

| Recurso | Para qué |
|---|---|
| `Namespace` | Aísla el proyecto del resto del clúster. La etiqueta `istio-injection: enabled` es la que activa Istio sobre estas cargas de trabajo |
| `ServiceAccount` | Identidad del pod. Es a lo que apuntan las políticas de Istio, en lugar de a una dirección IP |
| `Secret` | Credenciales fuera del manifiesto del Deployment. Base64 no es cifrado: por eso el real no se versiona |
| `Deployment` | Mantiene N réplicas vivas del contenedor y las reemplaza si caen |
| `Service` | IP virtual y nombre DNS estables delante de réplicas que van y vienen |
| `Gateway` | Punto de entrada desde Internet. En EKS, un balanceador de carga de AWS |
| `HTTPRoute` | Reglas de enrutamiento sobre ese punto de entrada: qué ruta va a qué Service |

## Equivalencias con el `docker-compose.yml` de la EP2

El mismo sistema descrito en las dos tecnologías. Sirve para ver qué aporta un orquestador de clúster frente a uno de un solo anfitrión.

| Docker Compose | Kubernetes | Diferencia |
|---|---|---|
| `services:` | `Deployment` | Compose corre un contenedor; el Deployment mantiene un número de réplicas y las reprograma si un nodo cae |
| `healthcheck:` | `readinessProbe` / `livenessProbe` / `startupProbe` | Compose tiene una sola noción de salud; Kubernetes separa "arrancando", "listo para tráfico" y "vivo", y actúa distinto en cada caso |
| `depends_on: service_healthy` | No tiene equivalente directo | En Kubernetes el orden se resuelve por convergencia: el pod reintenta hasta que la dependencia responde |
| `expose:` / `ports:` | `Service` + `HTTPRoute` | Compose publica un puerto del anfitrión; Kubernetes separa el descubrimiento interno de la exposición externa |
| `deploy.resources.limits` | `resources.requests` / `limits` | Kubernetes además usa `requests` para decidir en qué nodo cabe el pod |
| `--scale app=3` | `replicas: 3` | Manual en ambos, pero en Kubernetes el `HorizontalPodAutoscaler` lo automatiza según métricas |
| `env_file` / `.env` | `Secret` / `ConfigMap` | Recursos del clúster, con control de acceso propio, en lugar de un archivo en el disco del anfitrión |
| Un solo anfitrión | Varios nodos | Compose no reparte carga entre máquinas ni sobrevive a la caída del anfitrión |
