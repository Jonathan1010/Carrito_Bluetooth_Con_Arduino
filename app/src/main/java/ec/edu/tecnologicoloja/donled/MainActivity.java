package ec.edu.tecnologicoloja.donled;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button automatico, btnOff, adelante, izquierda, derecha, parar;
    Handler bluetoothIn;

    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // Servicio SPP UUID - esto debería funcionar para la mayoría de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Cadena para mac
    private static String address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Vinculamos los botones y textViews a las vistas respectivas
        automatico = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);

        adelante = (Button)findViewById(R.id.btnadelante);
        izquierda = (Button)findViewById(R.id.btnizquierda);
        derecha = (Button)findViewById(R.id.btnderecha);
        parar = (Button)findViewById(R.id.btnparar);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg){
                if (msg.what == handlerState) {
                    char MyCaracter = (char) msg.obj;

                    if(MyCaracter == 'w'){
                        //Si el dato es igual a 'w' el carrito avanza
                    }

                    if(MyCaracter == 'a'){
                        //Si el dato es igual a 'a' el carrito gira a la izquierda
                    }

                    if(MyCaracter == 'd'){
                        //Si el dato es igual a 'd' el carrito gira a la derecha
                    }

                    if(MyCaracter == 's'){
                        //Si el dato es igual a 's' el carrito retrocede
                    }

                    if(MyCaracter == 'p'){
                        //Si el dato es igual a 'p' el carrito se detiene
                    }
                }

            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        //apagar o parar el carro
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("P");    // envia p para parar el carro
                Toast.makeText(getBaseContext(), "El carrito esta parado", Toast.LENGTH_SHORT).show();
            }
        });


        //Enciende el carro en modo automatico
        automatico.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("E");    // envia e para poner el carro en modo automatico
                Toast.makeText(getBaseContext(), "Modo Automatico", Toast.LENGTH_SHORT).show();
            }
        });

        adelante.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("W");    // Envia "w" para ir hacia adelante el carro
                Toast.makeText(getBaseContext(), "Arrancar", Toast.LENGTH_SHORT).show();
            }
        });

        izquierda.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("A");    // Send "1" via Bluetooth
                Toast.makeText(getBaseContext(), "Giro a la izquierda", Toast.LENGTH_SHORT).show();
            }
        });

        derecha.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("D");    // Envia  d para el giro a la derecha
                Toast.makeText(getBaseContext(), "Giro a la derecha", Toast.LENGTH_SHORT).show();
            }
        });

        parar.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("S");    // Envia  s para la señal que retrocede el carro
                Toast.makeText(getBaseContext(), "Retrocediendo", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //crea una conexión saliente segura con el dispositivo BT utilizando el UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Obtener la dirección MAC de DeviceListActivity a través de intent
        Intent intent = getIntent();

        //Obtener la dirección MAC de la DeviceListActivty a través de EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        //Establece la conexión del "socket" Bluetooth.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //Envío un carácter al reanudar.la transmisión para comprobar que el dispositivo está conectado
        //Si no lo está se lanzará una excepción en el método de escritura y se llamará a finish()
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try{
            //No dejes las tomas de Bluetooth abiertas al salir de la actividad
            btSocket.close();
        } catch (IOException e2) {
            //
        }
    }

    //Comprueba que el Bluetooth del dispositivo Android está disponible y pide que se encienda si está desactivado
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //crear nueva clase para conectar el thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creación de la conexión thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Crear flujos de E/S para la conexión
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//leer bytes del buffer de entrada
                    String readMessage = new String(buffer, 0, bytes);
                    // Enviar los bytes obtenidos a la actividad de la interfaz de usuario a través del manejador via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //método de escritura (wtite)
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //convierte la cadena introducida en bytes
            try {
                mmOutStream.write(msgBuffer);                //escribir bytes a través de la conexión BT vía outstream
            } catch (IOException e) {
                //si no puede escribir, cierre la aplicación
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}