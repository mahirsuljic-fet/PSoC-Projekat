import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

df = pd.read_csv('output_data.csv')

def clean_value(val):
    val = str(val).lower().strip()
    if val.startswith('b'):
        try: return int(val[1:], 2)
        except: return 0
    try:
        return pd.to_numeric(val)
    except:
        return 0

signals = [col for col in df.columns if col != 'Timestamp']
for sig in signals:
    df[sig] = df[sig].apply(clean_value)

fig, axes = plt.subplots(len(signals), 1, figsize=(14, 2 * len(signals)), sharex=True)
if len(signals) == 1: axes = [axes]

for i, sig in enumerate(signals):
    data = df[sig]
    time = df['Timestamp']
    
    is_binary = data.max() <= 1 and data.min() >= 0 and data.nunique() <= 2
    
    if is_binary:
        axes[i].step(time, data, where='post', color='#0047AB', linewidth=2)
        axes[i].set_yticks([0, 1])
        axes[i].set_ylim(-0.2, 1.2)
    else:
        axes[i].step(time, data, where='post', color='#800000', linewidth=1.5)
        changes = np.where(data.values[:-1] != data.values[1:])[0]
        for idx in changes:
            axes[i].text(time.iloc[idx+1], data.iloc[idx+1], str(int(data.iloc[idx+1])), 
                         fontsize=9, verticalalignment='bottom', fontweight='bold')

    axes[i].set_ylabel(sig.split('.')[-1], rotation=0, labelpad=50, fontweight='bold', va='center')
    axes[i].grid(True, which='both', linestyle=':', alpha=0.4)
    axes[i].spines['top'].set_visible(False)
    axes[i].spines['right'].set_visible(False)

plt.xlabel('Vrijeme (ns)')
plt.suptitle('Zaustavljanje pri nailazku na prepreku', fontsize=16)
plt.tight_layout(rect=[0, 0.03, 1, 0.95])
plt.savefig('logic_plot.png', dpi=300)
plt.show()
