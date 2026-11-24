# Manifiestos de Istio

Istio es una **malla de servicios** (*service mesh*): una capa que se interpone en todo el tráfico entre contenedores sin que la aplicación lo sepa.

## Cómo funciona

Al crearse un pod en un namespace con la etiqueta `istio-injection: enabled`, Istio le inyecta un segundo contenedor: un proxy Envoy, el *sidecar*. Todo el tráfico de entrada y salida del pod pasa por ese proxy. De ahí salen las tres capacidades que se usan aquí:

- **Observabilidad sin instrumentar la aplicación.** El proxy ve cada petición, así que puede contar latencias, códigos de respuesta y volumen por servicio. El código Java no tiene una sola línea para esto.
- **Control de tráfico por identidad, no por IP.** Cada carga de trabajo recibe un certificado mTLS ligado a su `ServiceAccount`. Las políticas se escriben contra esa identidad, que no se puede falsificar cambiando de dirección.
- **Reparto de tráfico por peso.** Permite despliegues Canary, que Kubernetes por sí solo no puede hacer.

## Los dos ejercicios

### 1. [`01-authorization-policy.yaml`](01-authorization-policy.yaml) — control de acceso entre servicios

Restringe el acceso al puerto 3306 de MySQL a un único origen: la identidad del microservicio de autenticación.

Demostración: cambiar `action: ALLOW` por `action: DENY` y volver a aplicar.

```bash
kubectl apply -f istio/01-authorization-policy.yaml
kubectl logs -n ms-auth-user deployment/ms-auth-user -f
```

Los logs muestran el error de conexión a la base de datos y el enlace hacia MySQL aparece en rojo en el grafo de Kiali. Al devolver el valor a `ALLOW` y volver a aplicar, el tráfico se restablece **sin reiniciar ningún pod**.

Lo que demuestra: el estado del sistema se cambia editando un archivo versionado, no entrando a una máquina. El archivo es la única fuente de verdad.

### 2. [`02-canary.yaml`](02-canary.yaml) — despliegue Canary

Despliega una `v2` en paralelo a la `v1` y le envía solo el 10% del tráfico.

```bash
kubectl apply -f istio/02-canary.yaml
kubectl get pods -n ms-auth-user -L version
```

La progresión consiste en editar los pesos del `HTTPRoute` y volver a aplicar: `90/10` → `75/25` → `50/50` → `0/100`. Si la versión nueva se comporta mal, `100/0` revierte al instante.

Lo que demuestra: una versión nueva puede pasar todas las pruebas del pipeline y aun así fallar con tráfico real. El Canary acota el daño a una fracción de los usuarios y hace que la decisión de avanzar se tome con métricas, no con fe.

## Kiali

Kiali es la interfaz de la malla. Lee las métricas que Istio deposita en Prometheus y dibuja el grafo de servicios: qué habla con qué, con cuánta latencia, qué porcentaje de errores, y qué enlaces van cifrados con mTLS (el candado en cada arista).

Es la herramienta que materializa el tercer pilar de la observabilidad, las **trazas**: el recorrido de una petición a través de varios servicios. En un sistema de dos componentes aporta poco; en uno de decenas de microservicios es la diferencia entre encontrar el cuello de botella y adivinarlo.
