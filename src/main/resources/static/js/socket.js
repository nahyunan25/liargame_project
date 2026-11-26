// 웹소켓 공통 모듈: lobby.js, game.js(D 담당)는 이것만 써서 서버와 통신
//   LiarSocket.connect();                         // 접속
//   LiarSocket.on("CHAT", msg => { ... });        // 받은 메시지 종류별 처리
//   LiarSocket.send("CHAT", roomId, {text: "빨간색이에요"});
const LiarSocket = (() => {
  let ws = null;
  let myPlayerId = null;
  const handlers = {};

  function connect(onOpen) {
    const protocol = location.protocol === "https:" ? "wss" : "ws";
    ws = new WebSocket(`${protocol}://${location.host}/ws`);

    ws.onopen = () => {
      console.log("[socket] 연결됨");
      if (onOpen) onOpen();
    };

    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      if (msg.type === "CONNECTED") myPlayerId = msg.data.playerId;

      const handler = handlers[msg.type] || handlers["*"];
      if (handler) handler(msg);
      else console.log("[socket] 처리하지 않은 메시지", msg);
    };

    ws.onclose = () => console.log("[socket] 연결 끊김");
    ws.onerror = (e) => console.error("[socket] 오류", e);
  }

  function send(type, roomId = null, data = {}) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.warn("[socket] 아직 연결되지 않음:", type);
      return;
    }
    ws.send(JSON.stringify({ type, roomId, data }));
  }

  // 메시지 종류별 처리 함수 등록 ("*" = 나머지 전부)
  function on(type, handler) {
    handlers[type] = handler;
  }

  function getPlayerId() {
    return myPlayerId;
  }

  return { connect, send, on, getPlayerId };
})();