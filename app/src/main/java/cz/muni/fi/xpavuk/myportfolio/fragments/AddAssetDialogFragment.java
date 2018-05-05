package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.api.ApiEnum;

import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.DIGITAL_CURRENCY_DAILY;
import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.TIME_SERIES_DAILY;

/**
 * author: Tomas Pavuk
 * date: 5.5.2018
 */

public class AddAssetDialogFragment extends DialogFragment {

    private final CharSequence[] choices = {" Stock "," Crypto "};
    private ApiEnum.FUNCTION selectedAssetType = TIME_SERIES_DAILY;

    @BindView(R.id.dialog_asset_name)
    @Nullable
    EditText mDialogAssetName;
    @Nullable
    @BindView(R.id.dialog_asset_quantity)
    EditText mDialogAssetQuantity;
    @Nullable
    @BindView(R.id.radio_group)
    RadioGroup mDialogRadioGroup;

    public static final String CHOICE_SELECTED = "type";
    public static final String TICKER_SELECTED = "ticker";
    public static final String QUANTITY_SELECTED = "quantity";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View dialogView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        ButterKnife.bind(this, dialogView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        RadioButton rbStock = new RadioButton(getContext());
        rbStock.setText(choices[0]);
        RadioButton rbCrypto = new RadioButton(getContext());
        rbCrypto.setText(choices[1]);

        mDialogRadioGroup.addView(rbStock);
        mDialogRadioGroup.addView(rbCrypto);
        mDialogRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int childCount = group.getChildCount();
            for (int x = 0; x < childCount; x++) {
                RadioButton btn = (RadioButton) group.getChildAt(x);
                if (btn.getId() == checkedId) {
                    selectedAssetType = btn.getText().toString().equals(choices[0]) ? TIME_SERIES_DAILY : DIGITAL_CURRENCY_DAILY;
                }
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            Intent intent = new Intent();
            Bundle extras = new Bundle();
            extras.putString(TICKER_SELECTED, mDialogAssetName.getText().toString());
            extras.putString(QUANTITY_SELECTED, mDialogAssetQuantity.getText().toString());
            extras.putString(CHOICE_SELECTED, selectedAssetType.toString());
            intent.putExtras(extras);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }
}
