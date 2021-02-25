package ec.edu.tecnologicoloja.donled;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class DeviceListActivity extends Activity {
    //Depuración para LOGCAT
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    Button tlbutton;
    TextView textView1;

    // Cadena EXTRA para enviar a mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    //
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        //
        checkBTState();

        textView1 = (TextView) findViewById(R.id.connecting);
        textView1.setTextSize(40);
        textView1.setText(" ");

        //Inicializar el adaptador de matriz para los dispositivos emparejados
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        //Buscar y configurar el ListView para los dispositivos emparejados
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        //Obtener el adaptador Bluetooth local
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //Obtener un conjunto de dispositivos actualmente emparejados y añadirlo a 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        //Añadir dispositivos previamente emparejados al conjunto
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    //Configurar el oyente al hacer clic en la lista (mellado esto - inseguro)
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            textView1.setText("Conectando...");
            //Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres de la Vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //Haga un intento de iniciar la siguiente actividad mientras toma un extra que es la dirección MAC.
            Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    private void checkBTState() {
        //Comprueba que el dispositivo tiene Bluetooth y que está activado
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                //Pedir al usuario que encienda el Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}