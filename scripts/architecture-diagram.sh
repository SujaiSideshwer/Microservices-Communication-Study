#!/usr/bin/env bash
# ANSI-art diagram of the Order/Inventory sync+async communication flow.
# Horizontal layout: boxes flow left-to-right per lane so the whole thing
# fits in a single terminal screen without vertical scrolling.
# Run it with:  bash scripts/architecture-diagram.sh

RESET='\033[0m'
BOLD='\033[1m'
DIM='\033[2m'

BORDER='\033[1;97m'      # bright white borders
TITLE='\033[1;96m'       # cyan title
CLIENTC='\033[38;5;250m' # light grey
ORDERC='\033[38;5;81m'   # sky blue  -- order-service
INVC='\033[38;5;114m'    # green     -- inventory-service
SYNCC='\033[1;38;5;46m'  # bold green -- sync lane
ASYNCC='\033[1;38;5;213m' # bold magenta -- async lane
KAFKAC='\033[38;5;208m'  # orange -- kafka broker
NOTEC='\033[3;38;5;244m' # dim italic grey -- footnotes
ARROWC='\033[1;38;5;250m'

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

mkline() { printf "%${2}s" '' | tr ' ' "$1"; }

# render_box <outfile> <color> <width> <title> <content-line>...
render_box() {
  local outfile="$1" color="$2" width="$3" title="$4"; shift 4
  {
    printf "${BORDER}┌%s┐${RESET}\n" "$(mkline '─' $((width+2)))"
    printf "${BORDER}│${RESET} ${color}${BOLD}%-*s${RESET} ${BORDER}│${RESET}\n" "$width" "$title"
    local l
    for l in "$@"; do
      printf "${BORDER}│${RESET} ${color}%-*s${RESET} ${BORDER}│${RESET}\n" "$width" "$l"
    done
    printf "${BORDER}└%s┘${RESET}\n" "$(mkline '─' $((width+2)))"
  } > "$outfile"
}

# render_arrow <outfile> <height> <glyph>
render_arrow() {
  local outfile="$1" height="$2" glyph="$3" mid
  mid=$(( height / 2 ))
  {
    local i
    for ((i=0; i<height; i++)); do
      if [ "$i" -eq "$mid" ]; then printf "${ARROWC} %s ${RESET}\n" "$glyph"; else printf "     \n"; fi
    done
  } > "$outfile"
}

row() { paste -d '' "$@"; }   # merge same-height files side by side

echo
printf "${KAFKAC}  .--.${RESET}   ${BOLD}Kafka the Broker${RESET}${KAFKAC} -- \"one topic at a time, friends.\"${RESET}\n"
printf "${KAFKAC} ( oo )~~${RESET}\n"
printf "${KAFKAC}  \\\\/\\\\/${RESET}\n"
echo
printf "${TITLE}${BOLD}MICROSERVICES COMMUNICATION STUDY${RESET}  ${NOTEC}order-service :8080  <-->  inventory-service :8081${RESET}\n"
echo

# --------------------------------------------------------------- row 1 ----
# both boxes must share the same content-line count (3) so paste lines match
render_box "$TMP/r1_client" "$CLIENTC" 26 "CLIENT" "browser / curl" "" ""
render_arrow "$TMP/r1_a1" 6 "①─►"
render_box "$TMP/r1_order" "$ORDERC" 30 "ORDER-SERVICE :8080" \
  "OrderController" "POST /order/{productId}" "POST /order/confirm-payment"
row "$TMP/r1_client" "$TMP/r1_a1" "$TMP/r1_order"
printf "${NOTEC}   ① HTTP request  ->  CommunicationDispatcher.handle() routes by OrderEventType.mode() (compile-time, no runtime toggle)${RESET}\n"
echo

# --------------------------------------------------------------- row 2 ----
printf "${SYNCC}${BOLD}SYNC LANE :: ORDER_PLACEMENT${RESET}\n"
render_box "$TMP/r2_order" "$ORDERC" 20 "ORDER-SERVICE" "(sync lane)" "Dispatcher"
render_arrow "$TMP/r2_a1" 5 "②─►"
render_box "$TMP/r2_inv" "$INVC" 28 "INVENTORY-SERVICE :8081" \
  "GET  /inventory/{id}" "POST /inventory (qty-1)"
row "$TMP/r2_order" "$TMP/r2_a1" "$TMP/r2_inv"
printf "${NOTEC}   ② RestClient.get()/.post() -- BLOCKING call; reply: \"order placed successfully\" / \"product out of stock\"${RESET}\n"
echo

# --------------------------------------------------------------- row 3 ----
# all five boxes must share the same content-line count (2) so paste lines match
printf "${ASYNCC}${BOLD}ASYNC LANE :: PAYMENT_CONFIRMATION${RESET}\n"
render_box "$TMP/r3_order" "$ORDERC" 13 "ORDER-SVC" "(async lane)" "publish()"
render_arrow "$TMP/r3_a1" 5 "③─►"
render_box "$TMP/r3_k1" "$KAFKAC" 13 "KAFKA :9092" "payment-" "completed"
render_arrow "$TMP/r3_a2" 5 "④─►"
render_box "$TMP/r3_inv" "$INVC" 15 "INVENTORY-SVC" "reserveStock()" "@Transactional"
render_arrow "$TMP/r3_a3" 5 "⑤─►"
render_box "$TMP/r3_k2" "$KAFKAC" 13 "KAFKA :9092" "order-" "placed"
render_arrow "$TMP/r3_a4" 5 "⑥─►"
render_box "$TMP/r3_ordb" "$ORDERC" 15 "ORDER-SVC" "logs status" "(loop closed)"
row "$TMP/r3_order" "$TMP/r3_a1" "$TMP/r3_k1" "$TMP/r3_a2" "$TMP/r3_inv" "$TMP/r3_a3" "$TMP/r3_k2" "$TMP/r3_a4" "$TMP/r3_ordb"
printf "${NOTEC}   ③ kafkaTemplate.send() fire-and-forget   ④ @KafkaListener(group=inventory-service) -> reserveStock() @Transactional${RESET}\n"
printf "${NOTEC}   ⑤ builds OrderPlacedEvent, sends to order-placed   ⑥ @KafkaListener(group=order-service) -> logs status, loop closed${RESET}\n"
echo

printf "${NOTEC}* no Order entity/table anywhere -- \"placing an order\" IS the inventory check-and-decrement.${RESET}\n"
printf "${NOTEC}* @EnableFeignClients is on but unused -- all sync calls go through RestClient (an unused RestTemplate bean rides along too).${RESET}\n"
echo
