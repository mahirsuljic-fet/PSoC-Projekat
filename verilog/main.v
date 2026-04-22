module main (
    input wire clk,

    // movement controls
    input wire fwd_in,
    input wire bwd_in,
    input wire left_in,
    input wire right_in,
    input wire stoplight_in,
    input wire stopsign_in,
    input wire failsafe_in,

    // motors
    output wire [3:0] m1_out,
    output wire [3:0] m2_out,

    output wire [2:0] md_state,

    // parking sensors
    input  wire echo_fwd,
    output wire trig_fwd,
    // input  wire echo_bwd1,
    // output wire trig_bwd1,
    // input  wire echo_bwd2,
    // output wire trig_bwd2,

    // buzzer
    input  wire buzzer_in,
    output wire buzzer,

    // line detectors
    input wire ld_left,
    input wire ld_right,

    // enable pins
    input wire enable_ld,
    input wire enable_ps,
    input wire ps_sound,
    input wire ps_mode
);
  localparam PS_MODE_STOP = 0, PS_MODE_BEEP = 1;

  assign ps_mode_fwd = ps_mode == 1 ? PS_MODE_STOP : PS_MODE_BEEP;
  wire stop_in, ps_signal_fwd;

  assign stop_in = stopsign_in | stoplight_in | failsafe_in
                 | (ps_signal_fwd & (ps_mode_fwd == PS_MODE_STOP));
  assign buzzer = buzzer_in | (ps_signal_fwd & (ps_sound == 1 || ps_mode_fwd == PS_MODE_BEEP));

  motor_driver md (
      .clk(clk),
      .fwd_in(fwd_in),
      .bwd_in(bwd_in),
      .left_in(left_in),
      .right_in(right_in),
      .stop_in(stop_in),
      .ld_left(ld_left),
      .ld_right(ld_right),
      .m1_out(m1_out),
      .m2_out(m2_out),
      .state(md_state),
      .enable_ld(enable_ld)
  );

  parking_sensor ps_fwd (
      .clk(clk),
      .echo(echo_fwd),
      .trig(trig_fwd),
      .mode(ps_mode_fwd),
      .signal(ps_signal_fwd),
      .enable(enable_ps)
  );
endmodule
