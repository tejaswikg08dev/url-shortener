# Multi-stage build for React Frontend
# Stage 1: Build with Node (install deps, compile, bundle)
# Stage 2: Serve with Nginx (tiny, fast, production-ready)

# --- Stage 1: Build ---
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files first (dependency caching)
COPY package*.json ./
RUN npm ci
# npm ci = clean install from package-lock.json (reproducible builds)
# npm install = may update versions (not reproducible)

# Copy source and build
COPY . .
RUN npm run build
# Vite builds to /app/dist/ (static HTML, JS, CSS files)

# --- Stage 2: Serve ---
FROM nginx:alpine
# Nginx is a fast web server, perfect for serving static files
# Alpine variant is tiny (about 25MB)

# Copy built files to Nginx serve directory
COPY --from=build /app/dist /usr/share/nginx/html

# Copy our custom Nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Nginx runs as the main process
CMD ["nginx", "-g", "daemon off;"]