FROM node:18-alpine

WORKDIR /app

# Copy dependency manifests first (better caching)
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy app source
COPY . .

# ❌ REMOVE ENV PORT
# ❌ REMOVE hardcoded EXPOSE

# Expose is optional but harmless if generic
EXPOSE 8080

# Healthcheck uses injected PORT
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD node -e "require('http').get(`http://localhost:${process.env.PORT}/health`, r => process.exit(r.statusCode === 200 ? 0 : 1)).on('error', () => process.exit(1))"

CMD ["node", "src/server.js"]
