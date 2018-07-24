package com.soufienov.yoump3;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.soufienov.yoump3.util.PlayerConstants;

/**
 * Created by user on 12/07/2018.
 */

public class SettingsActivity extends Activity {

    RadioButton autoreplay,autonext;
    EditText maxres;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
autoreplay=(RadioButton)findViewById(R.id.radioButton);
autonext=(RadioButton)findViewById(R.id.radioButton2);
maxres=(EditText)findViewById(R.id.editText);
autonext.setChecked( PlayerConstants.AUTONEXT);
autoreplay.setChecked(PlayerConstants.AUTOREPLAY);
autonext.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        autoreplay.setChecked(false);

        PlayerConstants.AUTONEXT= !PlayerConstants.AUTONEXT;
        if(autonext.isChecked())
        PlayerConstants.AUTOREPLAY= false;

    }
});
        autoreplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autonext.setChecked(false);

                PlayerConstants.AUTOREPLAY= !PlayerConstants.AUTOREPLAY;
                if(autoreplay.isChecked())
                PlayerConstants.AUTONEXT= false;
            }
        });
      maxres.setText(PlayerConstants.MAXRES);

    }
}
