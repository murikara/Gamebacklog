package com.example.gamebacklog;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddGameActivity extends AppCompatActivity {

    @BindView(R.id.addTitelEditText)
    EditText mAddTitelEditText;

    @BindView(R.id.addPlatformEditText)
    EditText mAddPlatformEditText;

    @BindView(R.id.addNotesEditText)
    EditText mAddNotesEditText;

    @BindView(R.id.statusSpinner)
    Spinner mStatusSpinner;

    private ArrayAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.spinnerItems,
                 R.layout.support_simple_spinner_dropdown_item);
        mStatusSpinner.setAdapter(spinnerAdapter);
        Intent intent = getIntent();
        checkIfUpdate(intent);
    }

    /**
     * navigate back to home screen by finishing activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * Deze methode checkt of het gaat om een update of niet.
     * Zo ja, dan worden de input fields met informatie gevuld.
     * De informatie komt vanuit de recyclerview uit de main activity
     * @param intent is de intent waarvan de action wordt gecheckt
     */
    public void checkIfUpdate(Intent intent){
        if(intent.getAction().equals(MainActivity.ACTION_UPDATE)){
            Game game = intent.getParcelableExtra(MainActivity.EXTRA_GAME);
            mAddTitelEditText.setText(game.getTitel());
            mAddPlatformEditText.setText(game.getPlatform());
            mAddNotesEditText.setText(game.getNotes());
            int statusPosition = spinnerAdapter.getPosition(game.getStatus());
            mStatusSpinner.setSelection(statusPosition);
        }
    }

    /**
     * Deze methode voegt een nieuwe game item toe door het terug te sturen naar de main activity.
     * Op basis van de action van de meegegeven intent (vanuit main activity) wordt er gekeken of
     * een game geupdate of nieuw aangemaakt moet worden
     * @param view the view for snackbar
     */
    @OnClick(R.id.saveFloatingButton)
    public void addGame(View view) {
        //haal alle inputfields op
        String titel = mAddTitelEditText.getText().toString();
        String platform = mAddPlatformEditText.getText().toString();
        String notes = mAddNotesEditText.getText().toString();
        String status = mStatusSpinner.getSelectedItem().toString();

        //check of ze leeg zijn
        if (!TextUtils.isEmpty(titel) && !TextUtils.isEmpty(platform) && !TextUtils.isEmpty(status)) {
            Intent resultIntent = new Intent();
            resultIntent.setAction(getIntent().getAction());
            Game game;
            //getIntent verwijst naar de actieve intent ipv de nieuwe die hierboven wordt aangemaakt
            //er wordt vervolgens gecheckt of het om een update gaat
            if(getIntent().getAction().equals(MainActivity.ACTION_UPDATE)){
                //in geval van een update wordt het betreffende object gepakt om het te wijzigen
                //met de gewijzigde informatie
                game = getIntent().getParcelableExtra(MainActivity.EXTRA_GAME);
                game.setTitel(titel);
                game.setPlatform(platform);
                game.setNotes(notes);
                game.setStatus(status);
            } else {
                //als het niet om een update gaat, maak een nieuw game object aan
                game = new Game(titel, platform, notes, status);
            }
            resultIntent.putExtra(MainActivity.EXTRA_GAME, game);
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            Snackbar.make(view, "Enter some data first bro", Snackbar.LENGTH_LONG);
        }
        finish();
    }

}
