# xlsx-template-validator

This is an experimental project to learn more about keycloak's authentication services and apache poi. DO NOT use this as a template to use in production.

## Deployment

- Deploy keycloak in a k8s env with namespace 'keycloak'

- Import the realm using the exported file at the root dir

- Export already contains user alice with pwd alice

- cd into the helm dir, update the values file and run:
```
helm install xlsx-template-validator . -f values.yaml
```

## Steps for local development

- Build docker image
```
cd backend
./mvnw clean install
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t xlsx-validator .
```

- Start the keycloak service in k8s
```
helm install --set auth.adminPassword=authPassword keycloak oci://registry-1.docker.io/bitnamicharts/keycloak -n keycloak --create-namespace
```

- Add and ingress if needed
```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"networking.k8s.io/v1","kind":"Ingress","metadata":{"annotations":{},"name":"minimal-ingress","namespace":"keycloak"},"spec":{"ingressClassName":"nginx","rules":[{"host":"namansoracleapps.duckdns.org","http":{"paths":[{"backend":{"service":{"name":"keycloak","port":{"number":80}}},"path":"/","pathType":"Prefix"}]}}]}}
  creationTimestamp: "2023-05-15T00:45:05Z"
  generation: 1
  name: minimal-ingress
  namespace: keycloak
  resourceVersion: "12129"
  uid: 673aa22c-508a-46fb-bf54-53bd9f769a6a
spec:
  ingressClassName: nginx
  rules:
  - host: namansoracleapps.duckdns.org
    http:
      paths:
      - backend:
          service:
            name: keycloak
            port:
              number: 80
        path: /
        pathType: Prefix
status:
  loadBalancer:
    ingress:
    - ip: 192.168.0.101
```

- Import the realm using the exported file at the root dir

- Export already contains user alice with pwd alice

- Run the docker container
```
docker run -i --rm -p 8081:8080 -e QUARKUS_OIDC_AUTH_SERVER_URL='https://namansoracleapps.duckdns.org/realms/template-validator' -e TEMPLATE_DIR='/home/jboss/' -e CONSOLIDATION_DIR='/home/jboss/' -e QU
ARKUS_OIDC_AUTH_CLIENT_ID='backend-service' -e QUARKUS_OIDC_AUTH_SECRET='secret' xlsx-validator
```

- Start the frontend service
```
cd ../frontend
npm run dev
```

- Use the service in insecure google chrome instance [To avoid CORS]
```
mkdir -p /home/naman/test
google-chrome-stable --disable-web-security --user-data-dir="/home/naman/test"
```