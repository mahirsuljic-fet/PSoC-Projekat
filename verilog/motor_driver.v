module motor_driver (
    input wire clk,

    // movement controls from backend
    input wire fwd_in,
    input wire bwd_in,
    input wire left_in,
    input wire right_in,
    input wire stop_in,

    // line detectors
    input wire ld_left,
    input wire ld_right,

    // motors (A0 A1 B0 B1)
    output reg [3:0] m1_out,  // left
    output reg [3:0] m2_out,  // right
    output reg [2:0] state
);
  // states
  localparam STOP = 0, FORWARD = 1, BACKWARD = 2, LEFT = 3, RIGHT = 4;

  always @(posedge clk) begin
    // prioritize stop
    if (stop_in == 1) begin
      state = STOP;
    end else if (fwd_in == 1) begin
      // when moving forward check for lines
      if (ld_left == 1) begin
        state = RIGHT;
      end else if (ld_right == 1) begin
        state = LEFT;
      end else begin
        state = FORWARD;
      end
    end else if (bwd_in == 1) begin
      state = BACKWARD;
    end else if (right_in == 1) begin
      state = RIGHT;
    end else if (left_in == 1) begin
      state = LEFT;
    end else begin
      state = STOP;
    end
  end

  localparam M_FWD = 4'b1010;
  localparam M_BWD = 4'b0101;
  localparam M_STOP = 4'b0000;

  always @(state) begin
    case (state)
      FORWARD: begin
        m1_out = M_FWD;
        m2_out = M_FWD;
      end

      BACKWARD: begin
        m1_out = M_BWD;
        m2_out = M_BWD;
      end

      LEFT: begin
        m1_out = M_STOP;
        m2_out = M_FWD;
      end

      RIGHT: begin
        m1_out = M_FWD;
        m2_out = M_STOP;
      end

      STOP: begin
        m1_out = M_STOP;
        m2_out = M_STOP;
      end

      default: begin
        m1_out = M_STOP;
        m2_out = M_STOP;
      end
    endcase
  end

endmodule
