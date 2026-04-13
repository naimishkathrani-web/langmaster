import http from "node:http";
import { Server } from "socket.io";
import { createApp } from "./app.js";
import { env } from "./config.js";

const app = createApp();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

io.on("connection", (socket) => {
  socket.on("join-room", (roomId: string) => {
    socket.join(roomId);
  });

  socket.on("signal", (payload: { roomId: string; data: unknown }) => {
    socket.to(payload.roomId).emit("signal", payload.data);
  });

  socket.on("disconnect", () => {
    // no-op
  });
});

const port = env.PORT;
server.listen(port, () => {
  // eslint-disable-next-line no-console
  console.log(`LangMaster backend listening on :${port}`);
});
