package biz.binarysolutions.vatcalculator;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import biz.binarysolutions.vatcalculator.util.DefaultTextWatcher;

/**
 *
 */
public class Main extends AppCompatActivity
	implements
		OnItemClickListener,
		OnFocusChangeListener,
		OnItemSelectedListener {

	public static final String KEY_COUNTRY_INDEX = "countryIndex";
	public static final String KEY_RATE_INDEX    = "rateIndex";

	private JSONArray    jsonArray    = null;
	private List<String> countryNames = null;
	
	private double taxRate = 0;
	
	private Spinner spinnerRates;
	
	private EditText editTextBase;
	private EditText editTextTax;
	private EditText editTextTotal;

	private Button button;

	/**
	 *
	 */
	private final TextWatcher baseWatcher = new DefaultTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			double base = 0;

			try {
				base = Double.parseDouble(s.toString());
			} catch (Exception e) {
				// do nothing
			}

			double tax   = base * taxRate / 100.0;
			double total = base + tax;

			editTextTax.setText  ((tax   != 0)? getFormattedValue(tax)   : "");
			editTextTotal.setText((total != 0)? getFormattedValue(total) : "");
		}
	};

	/**
	 *
	 */
	private final TextWatcher taxWatcher = new DefaultTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			double tax = 0;

			try {
				tax = Double.parseDouble(s.toString());
			} catch (Exception e) {
				// do nothing
			}

			double base  = tax * 100.0 / taxRate;
			double total = base + tax;

			editTextBase.setText ((base  != 0)? getFormattedValue(base)  : "");
			editTextTotal.setText((total != 0)? getFormattedValue(total) : "");
		}
	};

	/**
	 *
	 */
	private final TextWatcher totalWatcher = new DefaultTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			double total = 0;

			try {
				total = Double.parseDouble(s.toString());
			} catch (Exception e) {
				// do nothing
			}

			double tax  = total * taxRate / (taxRate + 100.0);
			double base = total - tax;

			editTextBase.setText((base != 0)? getFormattedValue(base) : "");
			editTextTax.setText ((tax  != 0)? getFormattedValue(tax)  : "");
		}
	};

	/**
	 *
	 * @param value
	 * @return
	 */
	@SuppressWarnings("UnnecessaryLocalVariable")
	private String getFormattedValue(double value) {

		String formattedValue = new DecimalFormat("0.00").format(value);
		return formattedValue;
	}


	/**
	 *
	 * @return
	 */
	private JSONArray getJSONArray() {

		try {
			InputStream is     = getAssets().open("rates.json");
			int         size   = is.available();
			byte[]      buffer = new byte[size];

			//noinspection ResultOfMethodCallIgnored
			is.read(buffer);
			is.close();

			String json = new String(buffer, StandardCharsets.UTF_8);
			return new JSONArray(json);

		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 *
	 * @return
	 */
	private List<String> getCountryNames() {

		List<String> countryNames = new ArrayList<>();

		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray countryArray = jsonArray.getJSONArray(i);
				if (countryArray.length() > 0) {
					String countryName = countryArray.getString(0);
					countryNames.add(countryName);
				}
			}
		} catch (JSONException e) {
			// do nothing
		}

		return countryNames;
	}

	/**
	 *
	 * @return
	 */
	private List<String> getCountryRates(int position) {

		List<String> rates = new ArrayList<>();

		try {

			JSONArray countryArray = jsonArray.getJSONArray(position);
			JSONArray ratesArray   = countryArray.getJSONArray(1);

			for (int i = 0; i < ratesArray.length(); i++) {

				String rate = ratesArray.getString(i);
				rates.add(rate);
			}
		} catch (Exception e) {
			// do nothing
		}

		return rates;
	}

	/**
	 *
	 * @return
	 */
	private int getSavedCountryIndex() {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		return preferences.getInt(KEY_COUNTRY_INDEX, 0);
	}

	/**
	 *
	 * @return
	 */
	private int getSavedRateIndex() {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		return preferences.getInt(KEY_RATE_INDEX, 0);
	}

	/**
	 *
	 * @return
	 */
	private void saveCountryIndex(int index) {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		Editor editor = preferences.edit();
		editor.putInt(KEY_COUNTRY_INDEX, index);
		editor.apply();
	}

	/**
	 *
	 * @return
	 */
	private void saveRateIndex(int index) {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		Editor editor = preferences.edit();
		editor.putInt(KEY_RATE_INDEX, index);
		editor.apply();
	}

	/**
	 * 
	 */
	private void setCountrySpinner() {

		ArrayAdapter<String> adapter = new ArrayAdapter<>(
			this,
			android.R.layout.simple_dropdown_item_1line,
			countryNames
		);

		MaterialAutoCompleteTextView editText = findViewById(R.id.autoComplete);
		editText.setAdapter(adapter);
		editText.setOnItemClickListener(this);
		editText.setOnFocusChangeListener(this);

		String lastSavedCountry = countryNames.get(getSavedCountryIndex());
		editText.setText(lastSavedCountry);
	}
	
	/**
	 * 
	 */
	private void setRateSpinner() {

		ArrayAdapter<String> adapter = new ArrayAdapter<>(
			this,
			android.R.layout.simple_spinner_item,
			getCountryRates(getSavedCountryIndex())
		);

		adapter.setDropDownViewResource(
			android.R.layout.simple_spinner_dropdown_item
		);

		spinnerRates = findViewById(R.id.spinnerRates);
	    spinnerRates.setAdapter(adapter);
		spinnerRates.setOnItemSelectedListener(this);
		spinnerRates.setSelection(getSavedRateIndex());
	}	
	
	/**
	 * 
	 */
	private void setBaseListener() {
		
		editTextBase.setOnFocusChangeListener((v, hasFocus) -> {

			if (hasFocus) {
				editTextBase.addTextChangedListener(baseWatcher);
			} else {
				editTextBase.removeTextChangedListener(baseWatcher);
			}
		});
	}

	/**
	 * 
	 */
	private void setTaxListener() {
		
		editTextTax.setOnFocusChangeListener((v, hasFocus) -> {

			if (hasFocus) {
				editTextTax.addTextChangedListener(taxWatcher);
			} else {
				editTextTax.removeTextChangedListener(taxWatcher);
			}
		});
	}

	/**
	 * 
	 */
	private void setTotalListener() {
		
		editTextTotal.setOnFocusChangeListener((v, hasFocus) -> {

			if (hasFocus) {
				editTextTotal.addTextChangedListener(totalWatcher);
			} else {
				editTextTotal.removeTextChangedListener(totalWatcher);
			}
		});
	}

	/**
	 * 
	 */
	private void setEditTextListeners() {
		
		setBaseListener();
		setTaxListener();
		setTotalListener();
	}

	/**
	 * 
	 */
	private void deleteAll() {
		
		editTextBase.setText("");
		editTextTax.setText("");
		editTextTotal.setText("");
	}

	/**
	 * 
	 */
	private void setButtonListener() {
		button.setOnClickListener(v -> deleteAll());
	}

	/**
	 * 
	 * @param position
	 */
	private void updateSpinnerRatesAdapter(int position) {

		deleteAll();

		ArrayAdapter<String> adapter = new ArrayAdapter<>(
			this,
			android.R.layout.simple_spinner_item,
			getCountryRates(position)
		);

	    adapter.setDropDownViewResource(
			android.R.layout.simple_spinner_dropdown_item
		);
	    
	    spinnerRates.setAdapter(adapter);
	}

	/**
	 * 
	 * @param item
	 */
	private void updateTaxRate(String item) {

		deleteAll();
		
		try {
			taxRate = Double.parseDouble(item.substring(0, item.length() - 1)); 
		} catch (NumberFormatException e) {
			// do nothing
		}
	}

	/**
	 *
	 */
	private void initializeAds() {

		MobileAds.initialize(this);

		String           adId    = getString(R.string.admob_native_ad_id);
		AdLoader.Builder builder = new AdLoader.Builder(this, adId);
		builder.forNativeAd(nativeAd -> {

			TemplateView template = findViewById(R.id.my_template);
			template.setStyles(new NativeTemplateStyle.Builder().build());
			template.setNativeAd(nativeAd);
		});

		AdLoader adLoader = builder.build();
		adLoader.loadAd(new AdRequest.Builder().build());
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeAds();

        editTextBase  = findViewById(R.id.editTextBase);
        editTextTax   = findViewById(R.id.editTextTax);
		editTextTotal = findViewById(R.id.editTextTotal);

		button = findViewById(R.id.buttonClearAll);

		jsonArray    = getJSONArray();
		countryNames = getCountryNames();

        setCountrySpinner();
        setRateSpinner();
        
        setEditTextListeners();
        setButtonListener();
    }

	@Override
	public void onItemClick
		(
			AdapterView<?> parent,
			View           view,
			int            position,
			long           id
		) {

		if (! (view instanceof TextView textView)) {
			return;
		}

		String country      = textView.getText().toString();
		int    countryIndex = countryNames.indexOf(country);

		updateSpinnerRatesAdapter(countryIndex);

		saveCountryIndex(countryIndex);
		saveRateIndex(0);
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {

		if (view.getParent().getParent() instanceof TextInputLayout parent) {
			parent.setEndIconVisible(hasFocus);
		}

		if (hasFocus || !(view instanceof TextView textView)) {
			return;
		}

		String string = textView.getText().toString();
		if (countryNames.contains(string)) {
			return;
		}

		int countryIndex = getSavedCountryIndex();
		textView.setText(countryNames.get(countryIndex));
	}

	@Override
	public void onItemSelected
		(
			AdapterView<?> parent,
			View 		   view,
			int 		   position,
			long 		   id
		) {

		String item = (String) parent.getItemAtPosition(position);
		updateTaxRate(item);
		saveRateIndex(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// do nothing
	}
}