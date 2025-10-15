#!/bin/bash

# Script para iniciar las bases de datos locales de PostgreSQL

echo "🚀 Iniciando bases de datos PostgreSQL..."

# Iniciar primera instancia de PostgreSQL
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/logfile start

# Iniciar segunda instancia de PostgreSQL
pg_ctl -D /usr/local/var/postgres2 -l /usr/local/var/postgres2/logfile start

echo "✅ Bases de datos iniciadas"
