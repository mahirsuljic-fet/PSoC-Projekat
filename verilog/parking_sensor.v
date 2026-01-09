module parking_sensor (
    input  wire clk,
    input  wire mode,
    input  wire echo,
    output reg  trig,
    output reg  signal
);
  localparam PS_MODE_STOP = 0, PS_MODE_BEEP = 1;

  localparam CLK_FREQ = 50000000;
  localparam CYCLES_PER_CM = 2915;

  localparam DIST_CONST = 10 * CYCLES_PER_CM;
  localparam DIST_FAST = 15 * CYCLES_PER_CM;
  localparam DIST_SLOW = 20 * CYCLES_PER_CM;
  localparam DIST_STOP = 30 * CYCLES_PER_CM;

  localparam TIME_1S = CLK_FREQ;
  localparam TIME_500MS = CLK_FREQ / 2;
  localparam TIME_250MS = CLK_FREQ / 4;
  localparam TIME_125MS = CLK_FREQ / 8;

  localparam TIME_DETECT = TIME_125MS;
  localparam TIME_HOLD = 2 * TIME_1S;
  localparam TIME_COOLDOWN = 5 * TIME_1S;

  reg [21:0] trig_timer = 0;
  reg [21:0] echo_width = 0;
  reg [21:0] last_dist = 0;
  reg [27:0] toggle_timer = 0;
  reg detectable = 1;

  always @(posedge clk) begin
    // TRIGGER
    if (trig_timer < 4000000) trig_timer <= trig_timer + 1;
    else trig_timer <= 0;
    trig <= (trig_timer > 0 && trig_timer < 500);

    // ECHO
    if (echo == 1) echo_width <= echo_width + 1;
    else if (echo == 0 && echo_width > 0) begin
      last_dist  <= echo_width;
      echo_width <= 0;
    end

    // TOGGLE TIMER
    if (toggle_timer < CLK_FREQ) toggle_timer <= toggle_timer + 1;
    else toggle_timer <= 0;

    // PROXIMITY LOGIC
    if (mode == PS_MODE_STOP) begin
      if (detectable == 1) begin
        if (last_dist > DIST_STOP || last_dist == 0) begin
          signal <= 0;
          toggle_timer <= 0;
        end else begin
          if (signal == 0) begin
            if (toggle_timer > TIME_DETECT) begin
              signal <= 1;
              toggle_timer <= 0;
            end else begin
              toggle_timer <= toggle_timer + 1;
            end
          end else begin
            if (toggle_timer > TIME_HOLD) begin
              signal <= 0;
              detectable <= 0;
              toggle_timer <= 0;
            end else begin
              toggle_timer <= toggle_timer + 1;
            end
          end
        end
      end else begin
        if (toggle_timer > TIME_COOLDOWN) begin
          detectable   <= 1;
          toggle_timer <= 0;
        end else begin
          toggle_timer <= toggle_timer + 1;
        end
      end
    end else if (mode == PS_MODE_BEEP) begin
      if (last_dist > DIST_SLOW || last_dist == 0) begin
        signal <= 0;
      end else if (last_dist > DIST_FAST) begin
        signal <= (toggle_timer < TIME_500MS) ? 1 : 0;
      end else if (last_dist > DIST_CONST) begin
        signal <= (toggle_timer % (TIME_250MS * 2) < TIME_250MS) ? 1 : 0;
      end else begin
        signal <= 1;
      end
    end else begin
      signal <= 0;
    end
  end
endmodule

