package boyw165.com.my_mosaic_stickers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

import boyw165.com.my_mosaic_stickers.tool.ImageUtil;
import boyw165.com.my_mosaic_stickers.tool.LogUtils;
import boyw165.com.my_mosaic_stickers.view.CollageLayout;
import boyw165.com.my_mosaic_stickers.view.CollageMultiTouchListener;
import boyw165.com.my_mosaic_stickers.view.MosaicView;
import boyw165.com.my_mosaic_stickers.view.PopupMenu;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int OPEN_PHOTO_PICKER = 1;

    private ImageView mSampleScrap;
    private CollageLayout mCanvas;
    private PopupMenu mQuickMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSampleScrap = new ImageView(this);
        mSampleScrap.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mSampleScrap.setImageResource(R.drawable.sample_scrap);

        mCanvas = (CollageLayout) findViewById(R.id.canvas);
//        mCanvas.addViewToLayer1(mSampleScrap);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View quickMenuView = layoutInflater.inflate(R.layout.quick_menu, null);
        mQuickMenu = new PopupMenu(quickMenuView);
        mQuickMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.photo_picker: {
                        Intent intent = new Intent(Intent.ACTION_PICK);

                        intent.setType("image/*");
                        startActivityForResult(intent, OPEN_PHOTO_PICKER);
                        break;
                    }
                    case R.id.stickers_store: {
                        // TODO: Use factory pattern.
                        MosaicView mosaicView = new MosaicView(MainActivity.this);
                        mCanvas.addViewToLayer2(mosaicView);
                        break;
                    }
                    default:
                        LogUtils.log(TAG, "Default clicked!");
                }
            }
        });

        ImageButton button = (ImageButton) findViewById(R.id.add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mQuickMenu.showByView(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCanvas != null) {
            mCanvas.unsubscribeAll();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case OPEN_PHOTO_PICKER: {
                Uri uri = data.getData();
                // TODO: Create a ImageScrap class to handle this.
                ImageView imageView = new ImageView(this);
                Bitmap bitmap = ImageUtil.getSampledBitmap(this, uri);

                imageView.setImageBitmap(bitmap);
                imageView.setOnTouchListener(new CollageMultiTouchListener(this, 0.7f, 1.5f));

                mCanvas.addViewToLayer1(imageView);
                break;
            }
        }
    }
}
