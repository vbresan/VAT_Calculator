package biz.binarysolutions.vatcalculator;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.color.DynamicColors;
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
import biz.binarysolutions.vatcalculator.util.Spinner;

/**
 *
 */
public class Main extends AppCompatActivity implements OnFocusChangeListener {

	public static final String KEY_COUNTRY_INDEX = "countryIndex";
	public static final String KEY_RATE_INDEX    = "rateIndex";

	private JSONArray    jsonArray    = null;
	private List<String> countryNames = null;

	private double taxRate = 0;
	
	private Spinner spinnerRates;

	private TextInputLayout layoutBase;
	private TextInputLayout layoutTax;
	private TextInputLayout layoutTotal;

	private EditText editTextBase;
	private EditText editTextTax;
	private EditText editTextTotal;

	/**
	 *
	 */
	private final TextWatcher watcherBase = new DefaultTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			layoutBase.setStartIconVisible(editTextBase.length() > 0);

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
	private final TextWatcher watcherTax = new DefaultTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			layoutTax.setStartIconVisible(editTextTax.length() > 0);

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
	private final TextWatcher watcherTotal = new DefaultTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			layoutTotal.setStartIconVisible(editTextTotal.length() > 0);

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
	private void setJSONArray() {

		try {
			InputStream is     = getAssets().open("rates.json");
			int         size   = is.available();
			byte[]      buffer = new byte[size];

			//noinspection ResultOfMethodCallIgnored
			is.read(buffer);
			is.close();

			String json = new String(buffer, StandardCharsets.UTF_8);
			jsonArray = new JSONArray(json);

		} catch (Exception ex) {
			// do nothing
		}
	}

	/**
	 *
	 * @return
	 */
	private void setCountryNames() {

		countryNames = new ArrayList<>();

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
	}

	/**
	 *
	 * @return
	 */
	private List<String> getCountryRates(int countryIndex) {

		List<String> rates = new ArrayList<>();

		try {

			JSONArray countryArray = jsonArray.getJSONArray(countryIndex);
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
	private void onCountryItemClicked(String country) {

		clearCalculation();

		int index = countryNames.indexOf(country);
		spinnerRates.setAdapter(getCountryRates(index));
		saveCountryIndex(index);
		saveRateIndex(0);
	}

	/**
	 * 
	 */
	private void setCountrySpinner() {

		ArrayAdapter<String> adapter = new ArrayAdapter<>(
			this,
			R.layout.list_item,
			countryNames
		);

		MaterialAutoCompleteTextView view = findViewById(R.id.spinnerCountry);
		view.setAdapter(adapter);
		view.setOnFocusChangeListener(this);
		view.setOnItemClickListener((pa, v, po, id) ->
			onCountryItemClicked(view.getText().toString())
		);

		String lastSavedCountry = countryNames.get(getSavedCountryIndex());
		view.setText(lastSavedCountry);
	}

	/**
	 * 
	 */
	private void setRateSpinner() {

		spinnerRates = findViewById(R.id.spinnerRate);
		spinnerRates.setAdapter(getCountryRates(getSavedCountryIndex()));
		spinnerRates.setOnItemClickListener((p, v, position, i) ->
			saveRateIndex(position)
		);
		spinnerRates.addTextChangedListener(new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				updateTaxRate(editable.toString());
			}
		});

		int index = getSavedRateIndex();
		spinnerRates.setSelectedItem(index);
	}

	/**
	 *
	 * @param editText
	 * @param layout
	 * @param watcher
	 */
	private void setOnFocusChangeListener
		(
			EditText        editText,
			TextInputLayout layout,
			TextWatcher     watcher
		) {

		editText.setOnFocusChangeListener((v, hasFocus) -> {

			if (hasFocus) {
				layout.setStartIconVisible(editText.length() > 0);
				editText.addTextChangedListener(watcher);
			} else {
				layout.setStartIconVisible(false);
				editText.removeTextChangedListener(watcher);
			}
		});
	}
	
	/**
	 * 
	 */
	private void setEditTextListeners() {

		setOnFocusChangeListener(editTextBase,  layoutBase,  watcherBase);
		setOnFocusChangeListener(editTextTax,   layoutTax,   watcherTax);
		setOnFocusChangeListener(editTextTotal, layoutTotal, watcherTotal);
	}

	/**
	 * 
	 */
	private void clearCalculation() {
		
		editTextBase.setText("");
		editTextTax.setText("");
		editTextTotal.setText("");
	}

	/**
	 * 
	 * @param item
	 */
	private void updateTaxRate(String item) {

		clearCalculation();
		
		try {
			taxRate = Double.parseDouble(item.substring(0, item.length() - 1)); 
		} catch (NumberFormatException e) {
			// do nothing
		}
	}

	/**
	 *
	 */
	private void setStartIconListeners() {

		layoutBase.setStartIconOnClickListener(v -> editTextBase.setText(""));
		layoutTax.setStartIconOnClickListener(v -> editTextTax.setText(""));
		layoutTotal.setStartIconOnClickListener(v -> editTextTotal.setText(""));
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

	/**
	 *
	 */
	private void antiFlickWorkaround() {

		layoutTotal.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {

				layoutBase.setStartIconVisible(false);
				layoutTax.setStartIconVisible(false);
				layoutTotal.setStartIconVisible(false);

				layoutTotal.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DynamicColors.applyToActivityIfAvailable(this);
		setContentView(R.layout.main);

		setJSONArray();
		setCountryNames();

		layoutBase  = findViewById(R.id.layoutBase);
		layoutTax   = findViewById(R.id.layoutTax);
		layoutTotal = findViewById(R.id.layoutTotal);

		editTextBase  = findViewById(R.id.editTextBase);
		editTextTax   = findViewById(R.id.editTextTax);
		editTextTotal = findViewById(R.id.editTextTotal);

		setCountrySpinner();
		setRateSpinner();

		setStartIconListeners();
        setEditTextListeners();

		initializeAds();
		antiFlickWorkaround();
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
}