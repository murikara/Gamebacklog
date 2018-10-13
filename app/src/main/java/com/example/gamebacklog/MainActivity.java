package com.example.gamebacklog;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GameAdapter.GameClickListener {

    @BindView(R.id.addListButton)
    FloatingActionButton addListButton;

    @BindView(R.id.gameRecyclerView)
    RecyclerView gameRecyclerView;

    private GameAdapter gameAdapter;
    private List<Game> gameList;
    public static final int REQUESTCODE = 1234;
    public static final String EXTRA_GAME = "Game";
    private static AppDatabase db;

    //constante variabelen
    public final static int TASK_GET_ALL_GAMES = 0;
    public final static int TASK_DELETE_GAME = 1;
    public final static int TASK_UPDATE_GAME = 2;
    public final static int TASK_INSERT_GAME = 3;
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_INSERT = "insert";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        db = AppDatabase.getInstance(this);
        new GameAsyncTask(TASK_GET_ALL_GAMES).execute();
        gameRecyclerView.setAdapter(gameAdapter);
        gameRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        deleteGame();
    }

    /**
     * Deze methode maakt een nieuwe addgameactivity intent aan en zet de action van deze intent
     * op "update" zodat de addgameactivity weet dat het om een update gaat i.p.v. een nieuw object
     * @param i de cardview waarop is geklikt
     */
    @Override
    public void GameOnClick(int i) {
        Intent intent = new Intent(this, AddGameActivity.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_GAME, gameList.get(i));
        startActivityForResult(intent, REQUESTCODE);
    }

    /**
     * Deze methode opent addgameactivity voor het aanmaken van een nieuw game object
     */
    @OnClick(R.id.addListButton)
    public void navigateToAddGameActivity(){
        Intent intent = new Intent(this, AddGameActivity.class);
        intent.setAction(ACTION_INSERT);
        startActivityForResult(intent, REQUESTCODE);
    }

    /**
     * Update de UI
     */
    private void updateUI() {
        if (gameAdapter == null) {
            gameAdapter = new GameAdapter(gameList, this);
            gameRecyclerView.setAdapter(gameAdapter);
        } else {
            gameAdapter.swapList(gameList);
            gameAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Deze methode verwijdert een game als het wordt gewswiped naar links of rechts
     */
    private void deleteGame(){
        /*
        Add a touch helper to the RecyclerView to recognize when a user swipes to delete a list entry.
        An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
        and uses callbacks to signal when a user is performing these actions.
        */
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    //Called when a user swipes left or right on a ViewHolder
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        //Get the index corresponding to the selected position
                        int position = (viewHolder.getAdapterPosition());
                        new GameAsyncTask(TASK_DELETE_GAME).execute(gameList.get(position));
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(gameRecyclerView);
    }

    /**
     * Deze klasse handelt de database commando's af
     */
    public class GameAsyncTask extends AsyncTask<Game, Void, List> {

        private int taskCode;

        public GameAsyncTask(int taskCode) {
            this.taskCode = taskCode;
        }

        @Override
        protected List doInBackground(Game... games) {
            switch (taskCode){
                case TASK_DELETE_GAME:
                    db.gameDao().deleteGames(games[0]);
                    break;
                case TASK_UPDATE_GAME:
                    db.gameDao().updateGames(games[0]);
                    break;
                case TASK_INSERT_GAME:
                    db.gameDao().insertGames(games[0]);
                    break;
            }
            //To return a new list with the updated data, we get all the data from the database again.
            return db.gameDao().getAllGames();
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            onGameDbUpdated(list);
        }
    }

    /**
     * Deze methode wordt aangeroepen wanneer de database wordt geupdate
     * @param list de nieuwe geupdated lijst
     */
    public void onGameDbUpdated(List list) {
        gameList = list;
        updateUI();
    }

    /**
     * Deze methode ontvangt de data vanuit de addgameactivity op basis van de requestcode.
     * Vervolgens wordt de data gebruikt om een nieuwe insert te doen of een update.
     *
     * @param requestCode de requestcode
     * @param resultCode de resultcode
     * @param data de meegegeven data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Main", "I am back");
        if (requestCode == REQUESTCODE) {
            Log.e("Main", "Requestcode OKAY");
            if (resultCode == Activity.RESULT_OK) {
                Log.e("Main", "updating Game now");
                Game game = data.getParcelableExtra(MainActivity.EXTRA_GAME);
                Intent intent = data;
                Log.e("Main", intent.getAction());
                if(intent.getAction().equals(ACTION_INSERT)) {
                    new GameAsyncTask(TASK_INSERT_GAME).execute(game);
                } else if(intent.getAction().equals(ACTION_UPDATE)){
                    Log.e("update", "JAAA BRO");
                    new GameAsyncTask(TASK_UPDATE_GAME).execute(game);
                }
            }
        }
    }
}
