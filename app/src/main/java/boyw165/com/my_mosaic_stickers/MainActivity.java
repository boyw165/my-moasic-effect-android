package boyw165.com.my_mosaic_stickers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

import boyw165.com.my_mosaic_stickers.view.CollageLayout;
import boyw165.com.my_mosaic_stickers.view.MosaicView;

public class MainActivity extends AppCompatActivity {

    private ImageView mSampleScrap;
    private CollageLayout mCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSampleScrap = new ImageView(this);
        mSampleScrap.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mSampleScrap.setImageResource(R.drawable.bn_sample_scrap);

        mCanvas = (CollageLayout) findViewById(R.id.canvas);
        mCanvas.addViewToLayer1(mSampleScrap);

        ImageButton button = (ImageButton) findViewById(R.id.add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Bla bla bla...", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null)
//                        .show();

//                new SomeDialogFragment().show(getSupportFragmentManager(), "tag");

                // TODO: Use factory pattern.
                MosaicView mosaicView = new MosaicView(MainActivity.this);
                mCanvas.addViewToLayer2(mosaicView);
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
}
