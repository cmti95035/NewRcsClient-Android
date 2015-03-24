package mobisocial.musubi.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.validator.routines.InetAddressValidator;

import mobisocial.musubi.R;
import mobisocial.musubi.ui.SettingsActivity;

/**
 * Created by charlie on 3/12/15.
 */
public class IpSetDialog extends DialogFragment {

    EditText edIP;

    // Charlie: stop on persistent it to shared Pref
    public static final String ADS_SERVER_IP = "adsServerIP";

    public static IpSetDialog newInstance() {
        IpSetDialog frag = new IpSetDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setContentView(R.layout.ad_server_ip);
        d.setTitle(getResources().getString(R.string.set_server_ip));
        edIP = (EditText) d.findViewById(R.id.ads_server_ip);
        final SharedPreferences pref = getActivity().getSharedPreferences(
                SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String ip = pref.getString(ADS_SERVER_IP, "");
        edIP.setText(ip);

        Button btCancel = (Button) d.findViewById(R.id.stat_cancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button btOK = (Button) d.findViewById(R.id.stat_ok);
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // persistent the server ip
                if (!TextUtils.isEmpty(edIP.getText())) {
                    String ip = edIP.getText().toString();
                    if (InetAddressValidator.getInstance().isValid(ip)) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(ADS_SERVER_IP, ip);
                        editor.commit();
                        dismiss();
                    } else {
                        String msg = getResources().getString(R.string.wrong_ip);
                        Toast toast = Toast.makeText(v.getContext(), msg,
                                Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        });
        return d;
    }
}
