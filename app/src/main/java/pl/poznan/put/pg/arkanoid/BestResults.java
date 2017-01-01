package pl.poznan.put.pg.arkanoid;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class BestResults extends BaseActivity {

    private ListView listView;
    private ArrayAdapter<Result> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.best_results);

        listView = (ListView) findViewById(R.id.best_results_list_view);

        List<Result> results = readResults();

        if(results == null) {
            results = new ArrayList<>();
        }

        adapter = new ArrayAdapter<>(this, R.layout.best_result_item, results);

        listView.setAdapter(adapter);
    }
}
