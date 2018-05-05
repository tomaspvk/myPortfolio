package cz.muni.fi.xpavuk.myportfolio.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.fragments.StockListFragment;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar topToolbar;
    private AssetInterface listener;
    public void setListener(AssetInterface listener)
    {
        this.listener = listener ;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: FIND OUT WHY INIT IN APP.JAVA IS NOT WORKING
        Realm.init(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();

        if (savedInstanceState == null) {       // Important, otherwise there'd be a new Fragment created with every orientation change
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                StockListFragment slf = StockListFragment.newInstance();
                fragmentManager.beginTransaction()
                        .replace(android.R.id.content,
                                slf,
                                StockListFragment.class.getSimpleName())
                        .commit();
                setListener(slf);
            }
        }
    }

    private void setupToolbar(){
        topToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(topToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_addAsset:
                listener.action();
                return true;
            case R.id.action_refreshData:
                listener.onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}
