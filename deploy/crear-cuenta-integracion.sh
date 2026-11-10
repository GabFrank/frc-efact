#!/bin/bash
# Crea una cuenta de servicio + API key en FRC eFact producción y prueba la key con una consulta real.
# Correr desde la Mac en una terminal propia (pide datos):  bash deploy/crear-cuenta-integracion.sh
# El JWT no se muestra ni queda en el historial; la key se guarda en un archivo chmod 600.
set -euo pipefail

API="https://efact.frc-ecommerce.com/api"
CDC_PRUEBA="01800994825001002000003822025120417284341967"
DIR="$HOME/frc-efact-validacion"
mkdir -p "$DIR"

echo "Pegá tu auth_token (DevTools → Application → Local Storage → auth_token) y Enter:"
read -rs JWT; echo
[ -n "$JWT" ] || { echo "❌ Token vacío"; exit 1; }

read -rp "Nombre del sistema cliente [Bot de prueba FRC]: " NOMBRE; NOMBRE=${NOMBRE:-Bot de prueba FRC}
read -rp "RUC del cliente (opcional): " RUC
read -rp "Email de contacto (opcional): " EMAIL
read -rp "Nombre de la key [bot]: " NOMBRE_KEY; NOMBRE_KEY=${NOMBRE_KEY:-bot}
read -rp "Vence (AAAA-MM-DD, vacío = sin vencimiento): " VENCE

CUENTA_JSON=$(python3 -c 'import json,sys
n,r,e=sys.argv[1:4]
d={"nombre":n,"observacion":"creada con crear_cuenta_integracion.sh"}
if r: d["ruc"]=r
if e: d["emailContacto"]=e
print(json.dumps(d))' "$NOMBRE" "$RUC" "$EMAIL")

echo; echo "→ Creando cuenta de servicio..."
RESP=$(curl -sS -w '\n%{http_code}' "$API/admin/integracion/cuentas" \
  -H "Authorization: Bearer $JWT" -H 'Content-Type: application/json' -d "$CUENTA_JSON")
CODE=${RESP##*$'\n'}; BODY=${RESP%$'\n'*}
[ "$CODE" = "200" ] || [ "$CODE" = "201" ] || { echo "❌ HTTP $CODE: $BODY"; [ "$CODE" = "403" ] && echo "   (¿cerraste sesión tras recibir ADMIN? el token debe ser de gab)"; exit 1; }
CUENTA_ID=$(printf '%s' "$BODY" | python3 -c 'import json,sys;print(json.load(sys.stdin)["id"])')
echo "✅ Cuenta id=$CUENTA_ID ($NOMBRE)"

KEY_JSON=$(python3 -c 'import json,sys
n,v=sys.argv[1:3]
d={"nombre":n,"scopes":["de:consultar"]}
if v: d["expiraEn"]=v+"T23:59:59"
print(json.dumps(d))' "$NOMBRE_KEY" "$VENCE")

echo "→ Generando API key..."
RESP=$(curl -sS -w '\n%{http_code}' "$API/admin/integracion/cuentas/$CUENTA_ID/keys" \
  -H "Authorization: Bearer $JWT" -H 'Content-Type: application/json' -d "$KEY_JSON")
CODE=${RESP##*$'\n'}; BODY=${RESP%$'\n'*}
[ "$CODE" = "200" ] || [ "$CODE" = "201" ] || { echo "❌ HTTP $CODE: $BODY"; exit 1; }
unset JWT

ARCHIVO="$DIR/apikey_cuenta_${CUENTA_ID}.txt"
( umask 077; printf '%s' "$BODY" | python3 -c 'import json,sys;sys.stdout.write(json.load(sys.stdin)["clave"])' > "$ARCHIVO" )
PREFIJO=$(printf '%s' "$BODY" | python3 -c 'import json,sys;d=json.load(sys.stdin);print(d.get("key",{}).get("prefijo","?"))')
echo "✅ Key creada (prefijo $PREFIJO), guardada en $ARCHIVO"

echo "→ Probando la key con una consulta real a SIFEN (tarda ~10 s)..."
CODE=$(curl -sS -o "$DIR/prueba_cuenta_${CUENTA_ID}.json" -w '%{http_code}' --max-time 90 \
  -H "X-API-Key: $(cat "$ARCHIVO")" "$API/v1/integracion/documentos/$CDC_PRUEBA")
if [ "$CODE" = "200" ]; then
  python3 -c 'import json,sys;d=json.load(open(sys.argv[1]));print("✅ HTTP 200 ·",d["estado"],"·",d["emisor"]["razonSocial"],"·",d["timbrado"]["numeroCompleto"])' "$DIR/prueba_cuenta_${CUENTA_ID}.json"
else
  echo "⚠️ HTTP $CODE: $(cat "$DIR/prueba_cuenta_${CUENTA_ID}.json")"
fi

echo
echo "Listo. Cargá la key en el bot como FRC_EFACT_API_KEY desde el archivo, sin pegarla en ningún chat:"
echo "   $ARCHIVO"
