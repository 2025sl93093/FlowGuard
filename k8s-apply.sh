#!/bin/bash
# k8s-apply.sh — applies all Kubernetes manifests in the correct order
# Usage: ./k8s-apply.sh [your-dockerhub-username]

DOCKER_HUB_USER=${1:-REPLACE_WITH_YOUR_DOCKERHUB_USERNAME}

if [ "$DOCKER_HUB_USER" = "REPLACE_WITH_YOUR_DOCKERHUB_USERNAME" ]; then
  echo "Usage: ./k8s-apply.sh <your-dockerhub-username>"
  echo "Example: ./k8s-apply.sh johndoe"
  exit 1
fi

echo "Replacing DOCKER_HUB_USERNAME with: $DOCKER_HUB_USER"

# Replace placeholder in all deployment files
find k8s/ -name "*.yml" -exec sed -i.bak "s/DOCKER_HUB_USERNAME/$DOCKER_HUB_USER/g" {} \;
rm -f k8s/**/*.yml.bak k8s/**/**/*.yml.bak

echo "Applying Kubernetes manifests..."

kubectl apply -f k8s/namespace.yml
sleep 2

kubectl apply -f k8s/secrets/
kubectl apply -f k8s/storage/
sleep 2

kubectl apply -f k8s/postgres/
kubectl apply -f k8s/rabbitmq/
echo "Waiting for postgres and rabbitmq to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n flowguard --timeout=120s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n flowguard --timeout=120s

kubectl apply -f k8s/configmaps/
sleep 2

kubectl apply -f k8s/services/
echo "Waiting for application services..."
sleep 10

kubectl apply -f k8s/gateway/
kubectl apply -f k8s/dashboard/

echo ""
echo "All manifests applied. Check pod status:"
kubectl get pods -n flowguard

echo ""
echo "To access the dashboard:"
echo "  minikube service dashboard-service -n flowguard"
echo ""
echo "To access the API gateway:"
echo "  minikube service nginx-service -n flowguard"
