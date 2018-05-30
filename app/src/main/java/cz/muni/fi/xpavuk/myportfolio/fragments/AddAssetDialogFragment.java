package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.activities.MainActivity;
import cz.muni.fi.xpavuk.myportfolio.api.ApiEnum;

import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.DIGITAL_CURRENCY_DAILY;
import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.TIME_SERIES_DAILY;

/**
 * author: Tomas Pavuk
 * date: 5.5.2018
 */

public class AddAssetDialogFragment extends DialogFragment {

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

    @BindView(R.id.dialog_asset_name_wrapper)
    TextInputLayout mAssetNameWrapper;
    @BindView(R.id.dialog_asset_quantity_wrapper)
    TextInputLayout mAssetQuantity;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View dialogView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        ButterKnife.bind(this, dialogView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        RadioButton rbStock = new RadioButton(getContext());
        rbStock.setText(getString(R.string.stock));
        RadioButton rbCrypto = new RadioButton(getContext());
        rbCrypto.setText(getString(R.string.crypto));

        mDialogRadioGroup.addView(rbStock);
        mDialogRadioGroup.addView(rbCrypto);
        mDialogRadioGroup.check(rbStock.getId());

        mDialogRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int childCount = group.getChildCount();
            for (int x = 0; x < childCount; x++) {
                RadioButton btn = (RadioButton) group.getChildAt(x);
                if (btn.getId() == checkedId) {
                    selectedAssetType = btn.getText().toString().equals(getString(R.string.stock)) ? TIME_SERIES_DAILY : DIGITAL_CURRENCY_DAILY;
                }
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.add_button), (dialog, which) -> {
            if (!TextUtils.isEmpty(mDialogAssetName.getText()) && !TextUtils.isEmpty(mDialogAssetQuantity.getText()))
            {
                Intent intent = new Intent();
                Bundle extras = new Bundle();
                extras.putString("ticker", mDialogAssetName.getText().toString());
                extras.putString("quantity", mDialogAssetQuantity.getText().toString());
                extras.putString("type", selectedAssetType.toString());
                intent.putExtras(extras);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            } else
            {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button), (dialog, which) -> dialog.cancel());

        mAssetNameWrapper.setHint(getString(R.string.ticker));
        mAssetQuantity.setHint(getString(R.string.quantity));

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
