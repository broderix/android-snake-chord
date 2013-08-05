/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.snake;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.chord.ChordManager;
import com.samsung.chord.samples.apidemo.service.ChordSnakeService;
import com.samsung.chord.samples.apidemo.service.ChordSnakeService.ChordServiceBinder;
import com.samsung.chord.samples.apidemo.service.ChordSnakeService.IChordServiceListener;

/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a serpent roaming
 * around the garden looking for apples. Be careful, though, because when you catch one, not only
 * will you become longer, but you'll move faster. Running into yourself or the walls will end the
 * game.
 * 
 */
public class Snake extends Activity implements IChordServiceListener {

	private static final String TAG = "[Chord][ApiTest]";
    private static final String TAGClass = "SnakeActivity : ";
    private String mChannelName = "";
    private String mNodeName = "";
    /**
     * Constants for desired direction of moving the snake
     */
    public static int MOVE_LEFT = 0;
    public static int MOVE_UP = 1;
    public static int MOVE_DOWN = 2;
    public static int MOVE_RIGHT = 3;

    private static String ICICLE_KEY = "snake-view";

    private SnakeView mSnakeView;

    /**
     * Called when Activity is first created. Turns off the title bar, sets up the content views,
     * and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.snake_layout);

        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setDependentViews((TextView) findViewById(R.id.text),
                findViewById(R.id.arrowContainer), findViewById(R.id.background));

        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }
        mSnakeView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSnakeView.getGameState() == SnakeView.RUNNING) {
                    // Normalize x,y between 0 and 1
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();

                    // Direction will be [0,1,2,3] depending on quadrant
                    int direction = 0;
                    direction = (x > y) ? 1 : 0;
                    direction |= (x > 1 - y) ? 2 : 0;

                    // Direction is same as the quadrant which was clicked
                    mSnakeView.moveSnake(direction);
                    String message = String.valueOf(direction);
                    mChordService.sendDataToAll(mChannelName, message.getBytes());
                } else {
                    // If the game is not running then on touching any part of the screen
                    // we start the game by sending MOVE_UP signal to SnakeView
                    mSnakeView.moveSnake(MOVE_UP);
                }
                return false;
            }
        });
        
        startService();
        bindChordService();
    }

    private Activity getActivity() {
    	return Snake.this;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mSnakeView.setMode(SnakeView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

    /**
     * Handles key events in the game. Update the direction our snake is traveling based on the
     * DPAD.
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mSnakeView.moveSnake(MOVE_UP);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mSnakeView.moveSnake(MOVE_RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSnakeView.moveSnake(MOVE_DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mSnakeView.moveSnake(MOVE_LEFT);
                break;
        }

        return super.onKeyDown(keyCode, msg);
    }
 
    // **********************************************************************
    // Using Service
    // **********************************************************************
    private ChordSnakeService mChordService = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d(TAG, TAGClass + "onServiceConnected()");
            ChordServiceBinder binder = (ChordServiceBinder)service;
            mChordService = binder.getService();
            try {
                mChordService.initialize(Snake.this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int interfaceConnection = 0;
            for (int interfaceValue : mChordService.getAvailableInterfaceTypes()) {
                Log.d(TAG, TAGClass + "Available interface : " + interfaceValue);
                if (interfaceValue == ChordManager.INTERFACE_TYPE_WIFI) {
                	interfaceConnection = ChordManager.INTERFACE_TYPE_WIFI;
                	Log.d(TAG, TAGClass + "ChordManager.INTERFACE_TYPE_WIFI");
                	break;
                } else if (interfaceValue == ChordManager.INTERFACE_TYPE_WIFIAP) {
                	interfaceConnection = ChordManager.INTERFACE_TYPE_WIFIAP;
                	Log.d(TAG, TAGClass + "ChordManager.INTERFACE_TYPE_WIFIAP");
                	break;
                } else if (interfaceValue == ChordManager.INTERFACE_TYPE_WIFIP2P) {
                	interfaceConnection = ChordManager.INTERFACE_TYPE_WIFIP2P;
                	Log.d(TAG, TAGClass + "ChordManager.INTERFACE_TYPE_WIFIP2P");
                	break;
                }
            }
            int nError = mChordService.start(interfaceConnection);
            if (ChordManager.ERROR_NONE == nError) {
            	Toast.makeText(getActivity(), "Connection ok", Toast.LENGTH_SHORT).show();
            } else if (ChordManager.ERROR_INVALID_INTERFACE == nError) {
                Toast.makeText(getActivity(), "Invalid connection", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Fail to start", Toast.LENGTH_SHORT).show();
            }

            mChannelName = mChordService.getPublicChannel();
            mChordService.joinChannel(mChannelName);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.i(TAG, TAGClass + "onServiceDisconnected()");
            mChordService = null;
        }
    };
    public void bindChordService() {
        Log.i(TAG, TAGClass + "bindChordService()");
        if (mChordService == null) {
            Intent intent = new Intent(
                    "com.samsung.chord.samples.apidemo.service.ChordSnakeService.SERVICE_BIND");
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindChordService() {
        Log.i(TAG, TAGClass + "unbindChordService()");

        if (null != mChordService) {
            unbindService(mConnection);
        }
        mChordService = null;
    }

    private void startService() {
        Log.i(TAG, TAGClass + "startService()");
        Intent intent = new Intent("com.samsung.chord.samples.apidemo.service.ChordSnakeService.SERVICE_START");
        startService(intent);
    }

    private void stopService() {
        Log.i(TAG, TAGClass + "stopService()");
        Intent intent = new Intent("com.samsung.chord.samples.apidemo.service.ChordSnakeService.SERVICE_STOP");
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindChordService();
        stopService();
        Log.v(TAG, TAGClass + "onDestroy");
    }
    
	@Override
	public void onReceiveMessage(String node, String channel, String message) {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onReceiveMessage node="+node+" channel="+channel+" message="+message);
		int direction = Integer.parseInt(message);
		if (0 <= direction && direction <= 3) {
			mSnakeView.moveSnake(direction);
		}
	}

	@Override
	public void onFileWillReceive(String node, String channel, String fileName,
			String exchangeId) {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onFileWillReceive");
	}

	@Override
	public void onFileProgress(boolean bSend, String node, String channel,
			int progress, String exchangeId) {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onFileProgress");
	}

	@Override
	public void onFileCompleted(int reason, String node, String channel,
			String exchangeId, String fileName) {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onFileCompleted");
	}

	@Override
	public void onNodeEvent(String node, String channel, boolean bJoined) {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onNodeEvent node="+node+" channel="+channel+" bJoined="+bJoined);
		mNodeName = node;
	}

	@Override
	public void onNetworkDisconnected() {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onNetworkDisconnected");
	}

	@Override
	public void onUpdateNodeInfo(String nodeName, String ipAddress) {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onUpdateNodeInfo");
	}

	@Override
	public void onConnectivityChanged() {
		// TODO Auto-generated method stub
		Log.v(TAG, TAGClass + "onConnectivityChanged");
	}
}
