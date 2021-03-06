package cz.muni.fi.xpavuk.myportfolio.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.fragments.StockListFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar topToolbar;
    private AssetInterface listener;
    private StockListFragment stockListFragment;
    public void setListener(AssetInterface listener)
    {
        this.listener = listener ;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();

        if (savedInstanceState == null) {       // Important, otherwise there'd be a new Fragment created with every orientation change
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                stockListFragment = StockListFragment.newInstance();
                fragmentManager.beginTransaction()
                        .replace(android.R.id.content,
                                stockListFragment,
                                StockListFragment.class.getSimpleName())
                        .commit();
            }
        } else
            stockListFragment = (StockListFragment) getSupportFragmentManager().findFragmentByTag(StockListFragment.class.getSimpleName());
        setListener(stockListFragment);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void switchContent(int id, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(id, fragment, fragment.toString());
        ft.addToBackStack(null);
        ft.commit();
    }
}