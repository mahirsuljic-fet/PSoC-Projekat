from vcdvcd import VCDVCD
import csv
import sys

vcd_file = '../verilog/sim.vcd'
try:
    vcd = VCDVCD(vcd_file)
except Exception as e:
    print(f"Error loading VCD: {e}")
    sys.exit(1)

prefix = 'ultrasonic_stop_test.uut.'

signals_to_export = [
    prefix + 'echo_fwd',
    prefix + 'trig_fwd',
    prefix + 'stop_in'
]

all_timestamps = set()
found_signals = []

print("Searching for signals...")
for sig_name in signals_to_export:
    if sig_name in vcd.signals:
        found_signals.append(sig_name)
        for t, _ in vcd[sig_name].tv:
            all_timestamps.add(t)
    else:
        print(f"  [!] Warning: '{sig_name}' not found.")

if not found_signals:
    print("\nNo matching signals found! Here are the signals available in your VCD:")
    for s in vcd.signals:
        print(f"  {s}")
    sys.exit(1)

sorted_times = sorted(list(all_timestamps))

output_file = 'output_data.csv'
with open(output_file, 'w', newline='') as f:
    writer = csv.writer(f)

    row = ['Timestamp']
    for sig in found_signals:
        row.append(sig.removeprefix(prefix))
    writer.writerow(row)
    
    for t in sorted_times:
        row = [t]
        for sig_name in found_signals:
            row.append(vcd[sig_name][t])
        writer.writerow(row)

print(f"\nSuccess! Created {output_file} with {len(sorted_times)} rows.")
