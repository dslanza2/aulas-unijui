package com.aula.carrinho;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import com.aula.carrinho.utils.Alert;
import com.aula.carrinho.v2.CarrinhoV2View;
import com.aula.carrinho.v3.CarrinhoV3View;
import com.utils.LogMod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.khronos.opengles.GL10;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.NativeCameraView;
import org.opencv.android.OpenCVLoader;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 00:06:23 - 11.07.2010
 */
public class TelaActivity extends BaseGameActivity {
    // ===========================================================
    // Constants
    // ===========================================================

    private String TAG = "CARRINHO PRE-lOAD";
    private static final int CAMERA_WIDTH = 180;
    private static final int CAMERA_HEIGHT = 320;
    private static final int DIALOG_ALLOWDIAGONAL_ID = 0;
    // ===========================================================
    // Fields
    // ===========================================================
    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private BitmapTextureAtlas mOnScreenControlTexture;
    private TextureRegion mOnScreenControlBaseTextureRegion;
    private TextureRegion mOnScreenControlKnobTextureRegion;
    private DigitalOnScreenControl mDigitalOnScreenControl;
    private static BluetoothSocket bluetoothSocket;
    private boolean procurando = false;
    private IOnScreenControlListener ionScreenControlListener;
    private CarrinhoV3View carrinhoV3View;
    private NativeCameraView nativeCameraView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nativeCameraView != null) {
            nativeCameraView.disableView();
        }
    }
  

    @Override
    protected void onStart() {
        super.onStart();
        if (!ParametrosActivity.manual) {
            if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mOpenCVCallBack)) {
                Log.e(TAG, "Cannot connect to OpenCV Manager");
            }
        }
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
    }

    private void carregaBluetooth() {
        if (procurando) {
            return;
        } else {
            procurando = true;
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            LogMod.e("CARRINHO", "NÂO PEGO O BLUETOOTH", null);
//            Alert alert = new Alert(this);
//            alert.setMessage("HAAA HAAA.. Tu não tem Bluetooth");
//            alert.addButton(" Então Tá.", new DialogInterface.OnClickListener() {
//
//                public void onClick(DialogInterface di, int i) {
////                    finish();
//                }
//            });
//            alert.show();

        } else {
            try {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 0);
                }

                BluetoothDevice carrinho = null;
                //
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        if (device.getAddress().equals("00:11:05:09:00:99")) {
                            carrinho = device;
                        }
                    }
                }
                if (carrinho == null) {
                    Alert alert = new Alert(this);
                    alert.setMessage("Da uma olhada que ta Desligado o Carrinho.");
                    alert.addButton(" Então Tá.", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface di, int i) {
//                    finish();
                        }
                    });
                    alert.show();

//                    return;
                }
                //21:03:30.026	225	#225	DEBUG	BluetoothService	    uuid(application): 00001101-0000-1000-8000-00805f9b34fb 1
                mBluetoothAdapter.cancelDiscovery();

                UUID MY_UUID =
                        UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                LogMod.i("CARRINHO", MY_UUID.toString());
                if (bluetoothSocket == null) {
                    bluetoothSocket = carrinho.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                } else {
                    if (!bluetoothSocket.isConnected()) {
                        bluetoothSocket.connect();
                    }
                }
                LogMod.i("CARRINHO", "DEU CERTO ....");
                new Thread(new Runnable() {
                    public void run() {
                        boolean ativo = true;
                        while (ativo) {
                            try {
                                InputStream inputStream = bluetoothSocket.getInputStream();
//                                BufferedInputStream bis = new BufferedInputStream(inputStream);
                                InputStreamReader reader = new InputStreamReader(inputStream);
                                BufferedReader br = new BufferedReader(reader);
                                LogMod.i("ARDUINO", br.readLine());
                            } catch (IOException ex) {
                                Logger.getLogger(TelaActivity.class.getName()).log(Level.SEVERE, null, ex);
                                ativo = false;
                            }

                        }
                    }
                }).start();
                ////
                //this.mDigitalOnScreenControl = new DigitalOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {



            } catch (Exception ex) {
                LogMod.e("CARRINHO", "FUDEU ao Conectar", ex);
            } finally {
                procurando = false;

            }


        }
    }

    @Override
    public Engine onLoadEngine() {
//        carregaBluetooth();
        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
    }

    @Override
    public void onLoadResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.mBitmapTextureAtlas = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
//		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);

        this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
        this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

        this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas, this.mOnScreenControlTexture);
    }

    @Override
    public Scene onLoadScene() {
        carregaBluetooth();
        LogMod.init();
        this.mEngine.registerUpdateHandler(new FPSLogger());

        final Scene scene = new Scene();
        scene.setBackground(new ColorBackground(0.0f, 0.0f, 0.0f));

        ionScreenControlListener = new IOnScreenControlListener() {
            @Override
            public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {

                if (!ParametrosActivity.manual) {
                    return;
                }
                String tecla = "x";
                if (pValueY > 0) {
                    tecla = "s";
                } else if (pValueY < 0) {
                    tecla = "w";
                } else if (pValueX > 0) {
                    tecla = "d";
                } else if (pValueX < 0) {
                    tecla = "a";
                }
                if (!tecla.equals("")) {
                    try {
                        LogMod.i("CARRINHO", tecla);
                        if (bluetoothSocket != null) {
                            tecla += "\n";
                            bluetoothSocket.getOutputStream().write(tecla.getBytes());
                        }

                    } catch (IOException ex) {
//                        LogMod.e("CARRINHO", "Fudeu quando mandou o camndo", ex);
                    }
                }
            }
        };
        this.mDigitalOnScreenControl = new DigitalOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, ionScreenControlListener);

        this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        this.mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
        this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
        this.mDigitalOnScreenControl.getControlBase().setScale(1.25f);
        this.mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
        this.mDigitalOnScreenControl.refreshControlKnobPosition();

        scene.setChildScene(this.mDigitalOnScreenControl);

        return scene;
    }
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Create and set View
                    if (carrinhoV3View == null) {
                        nativeCameraView= new NativeCameraView(TelaActivity.this, null);                        
                        carrinhoV3View = new CarrinhoV3View(TelaActivity.this);
                        nativeCameraView.setCvCameraViewListener(carrinhoV3View);
                        setContentView(nativeCameraView);
                        nativeCameraView.enableView();
                    }

                    // Check native OpenCV camera
//                    if (eraNull && !carrinhoV3View.openCamera()) {
//                        AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
//                        ad.setCancelable(false); // This blocks the 'BACK' button
//                        ad.setMessage("Fatal error: can't open camera!");
//                        ad.setButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                finish();
//                            }
//                        });
//                        ad.show();
//                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onSetContentView() {
        final FrameLayout frameLayout = new FrameLayout(this);
        final FrameLayout.LayoutParams frameLayoutLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);

        this.mRenderSurfaceView = new RenderSurfaceView(this);
        mRenderSurfaceView.setRenderer(mEngine);
        final FrameLayout.LayoutParams surfaceViewLayoutParams = new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());
        frameLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
        LogMod.init();
        LogMod.i("CARRINHO", "CRUZEI SEtCONTENTVIEW");

        this.setContentView(frameLayout, frameLayoutLayoutParams);
    }

    @Override
    public void onLoadComplete() {
        this.showDialog(DIALOG_ALLOWDIAGONAL_ID);
    }
    // ===========================================================
    // Methods
    // ===========================================================
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public void enviarCamera(int valor) {
        try {
            String tecla = "c" + valor + "\n";
            System.out.println("Mandei: " + tecla);
            bluetoothSocket.getOutputStream().write(tecla.getBytes());
            Thread.sleep(100);
        } catch (Exception ex) {
            LogMod.e("CARRINHO", "Erro ao mandar comando ", ex);
            LogMod.i("CARRINHO", "Tentando conectar de novo...");
            try {
                bluetoothSocket.connect();
            } catch (IOException ex1) {
                LogMod.e("CARRINHO", "Erro ao reconectar", ex1);
            }
        }
    }

    public void enviarPotencia(int potenciaEsquerda, int potenciaDireita) {
        String pe;
        String pd;
        if (potenciaEsquerda > 0) {
            if (potenciaEsquerda < 10) {
                pe = "+0" + potenciaEsquerda;
            } else {
                pe = "+" + potenciaEsquerda;
            }
        } else if (potenciaEsquerda < 0) {
            if (potenciaEsquerda > -10) {
                pe = "-0" + String.valueOf(Math.abs(potenciaEsquerda));
            } else {
                pe = String.valueOf(potenciaEsquerda);
            }
        } else {
            pe = String.valueOf("+00");
        }
        if (potenciaDireita > 0) {
            if (potenciaDireita < 10) {
                pd = "+0" + potenciaDireita;
            } else {
                pd = "+" + potenciaDireita;
            }
        } else if (potenciaDireita < 0) {
            if (potenciaDireita > -10) {
                pd = "-0" + String.valueOf(Math.abs(potenciaDireita));
            } else {
                pd = String.valueOf(potenciaDireita);
            }
        } else {
            pd = String.valueOf("+00");
        }
        String tecla = pd + pe;
        System.out.println("Tentando mandar... " + tecla);
        if (bluetoothSocket != null) {
            try {
                tecla += "\n";
                System.out.println("Mandei: " + tecla);
                bluetoothSocket.getOutputStream().write(tecla.getBytes());
                Thread.sleep(100);
            } catch (Exception ex) {
//                LogMod.e("CARRINHO", "Erro ao mandar comando ", ex);
//                LogMod.i("CARRINHO", "Tentando conectar de novo...");
//                try {
////                    bluetoothSocket.connect();
//                } catch (IOException ex1) {
////                    LogMod.e("CARRINHO", "Erro ao reconectar", ex1);
//                }
            }
        } else {
            System.out.println("Falhou");
        }
    }

    @Override
    protected void onPause() {
        enviarPotencia(0, 0);
        super.onPause();
        if (null != nativeCameraView) {
            nativeCameraView.disableView();
        }
    }

    @Override
    protected void onStop() {
        enviarPotencia(0, 0);
        super.onStop();
    }
}
