# Guía de Despliegue — cc-identity-service

## Requisitos previos

- Azure Subscription con App Service Linux
- GitHub repo con Actions habilitado
- PAT de GitHub con scope `read:packages`
- MongoDB Atlas o instancia accesible

---

## 1. Secrets del repositorio (GitHub → Settings → Secrets and variables → Actions)

| Secret | Descripción |
|---|---|
| `AZURE_WEBAPP_NAME` | Nombre del App Service en Azure |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Publish profile del App Service (descargar desde Azure Portal) |

## 2. Crear App Service en Azure

```bash
# Grupo de recursos
az group create --name techcup-identity-rg --location eastus

# App Service Plan Linux (mínimo B1 para contenedores)
az appservice plan create \
  --name techcup-identity-plan \
  --resource-group techcup-identity-rg \
  --sku B1 \
  --is-linux

# Web App con contenedor
az webapp create \
  --resource-group techcup-identity-rg \
  --plan techcup-identity-plan \
  --name <nombre-unico> \
  --deployment-container-image-name ghcr.io/tech-cup-2026-int/cc-identity-service:latest
```

## 3. Configurar contenedor en Azure Portal

| Campo | Valor |
|---|---|
| Image Source | `Other container registries` |
| Access Type | `Private` |
| Registry server URL | `https://ghcr.io` |
| Username | `TECH-CUP-2026-INT` |
| Password | PAT con `read:packages` |
| Image and tag | `ghcr.io/tech-cup-2026-int/cc-identity-service:latest` |
| Startup Command | *(vacío)* |

## 4. Variables de entorno (App Settings)

```bash
az webapp config appsettings set \
  --resource-group techcup-identity-rg \
  --name <nombre-unico> \
  --settings \
    WEBSITES_PORT=8081 \
    MONGODB_URI="mongodb+srv://..." \
    GOOGLE_CLIENT_ID="..." \
    GOOGLE_CLIENT_SECRET="..." \
    JWT_SECRET="..."
```

## 5. Trigger del deploy

Hacer push a `main` — el workflow de GitHub Actions:

```
push a main → build-test → dockerize-publish → deploy a Azure
```

La imagen se publica en:
```
ghcr.io/tech-cup-2026-int/cc-identity-service:latest
```

## 6. Verificar

```
https://<nombre-unico>.azurewebsites.net/actuator/health
https://<nombre-unico>.azurewebsites.net/swagger-ui/index.html
```
