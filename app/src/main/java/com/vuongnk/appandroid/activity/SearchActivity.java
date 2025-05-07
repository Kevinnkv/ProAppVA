package com.vuongnk.appandroid.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.BookAdapter;
import com.vuongnk.appandroid.model.Book;
import com.vuongnk.appandroid.model.Category;
import com.vuongnk.appandroid.util.GridSpacingItemDecoration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView etSearch;
    private RecyclerView rcvSearchResults;
    private BookAdapter bookAdapter;
    private List<Book> allBooks;
    private List<Book> filteredBooks;
    private List<Category> categoryList;
    private DatabaseReference databaseReference, categoryRef;
    private ImageView imgBack, imgSearch;
    private TextView tvResultCount;
    private View layoutEmpty;
    private Spinner spinnerSort, spinnerCategory;

    // Debounce variables
    private static final long DEBOUNCE_DELAY = 300;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initUI();
        initFirebase();
        loadAllBooks();
        getCategories();
        setupListeners();
        setupSortAndFilterOptions();
    }

    private void initUI() {
        etSearch = findViewById(R.id.etSearch);
        rcvSearchResults = findViewById(R.id.rcvSearchResults);
        imgBack = findViewById(R.id.imgBack);
        imgSearch = findViewById(R.id.imgSearch);
        tvResultCount = findViewById(R.id.tvResultCount);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        allBooks = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        categoryList = new ArrayList<>();

        bookAdapter = new BookAdapter(this, filteredBooks);
        rcvSearchResults.setAdapter(bookAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rcvSearchResults.setLayoutManager(layoutManager);
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing);
        rcvSearchResults.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
    }

    private void initFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("books");
        categoryRef = database.getReference("categories");
    }

    private void loadAllBooks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allBooks.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null && book.isActive() == 0) {
                        allBooks.add(book);
                    }
                }
                setupSearchSuggestions();
                filterBooks(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SearchActivity.this,
                        "Lỗi khi tải dữ liệu: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCategories() {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear(); // Xóa danh sách cũ

                // Thêm category mặc định "Tất cả"
                Category allCategory = new Category();
                allCategory.setName("Tất cả");
                allCategory.setId("All");
                categoryList.add(allCategory);

                // Lấy các category từ Firebase
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        category.setId(snapshot.getKey());
                        categoryList.add(category);
                    }
                }

                // Cập nhật Spinner category
                updateCategorySpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this,
                        "Lỗi khi tải danh mục: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupSortAndFilterOptions() {
        // Loại bỏ phần setup category từ resource

        // Thiết lập Spinner sắp xếp
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        // Sự kiện thay đổi sắp xếp
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortBooks(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sự kiện lọc theo thể loại
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchSuggestions() {
        List<String> suggestions = generateSearchSuggestions();
        ArrayAdapter<String> suggestionsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );
        etSearch.setAdapter(suggestionsAdapter);
    }

    private List<String> generateSearchSuggestions() {
        Set<String> suggestionSet = new HashSet<>();
        for (Book book : allBooks) {
            suggestionSet.add(book.getTitle());
            suggestionSet.add(book.getAuthor());

            // Tách từ từ tiêu đề và mô tả để tạo gợi ý
            String[] titleWords = book.getTitle().split("\\s+");
            String[] descWords = book.getDescription().split("\\s+");

            for (String word : titleWords) {
                if (word.length() > 2) suggestionSet.add(word);
            }
            for (String word : descWords) {
                if (word.length() > 2) suggestionSet.add(word);
            }
        }
        return new ArrayList<>(suggestionSet);
    }

    private void setupListeners() {
        imgBack.setOnClickListener(v -> finish());

        imgSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            filterBooksImmediately(query);
        });

        // Xử lý khi nhấn nút search trên bàn phím
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                filterBooksImmediately(query);
                return true;
            }
            return false;
        });

        // Áp dụng Debounce khi text thay đổi
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Loại bỏ runnable cũ
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Tạo runnable mới với delay
                searchRunnable = () -> {
                    String query = s.toString().toLowerCase().trim();
                    filterBooks(query);
                };
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sortBooks(int sortOption) {
        if (filteredBooks.isEmpty()) return;

        switch (sortOption) {
            case 0: // Mặc định
                break;
            case 1: // Sắp xếp theo điểm đánh giá giảm dần
                Collections.sort(filteredBooks, (b1, b2) ->
                        Double.compare(b2.getAverageRating(), b1.getAverageRating()));
                break;
            case 2: // Sắp xếp theo giá tăng dần
                Collections.sort(filteredBooks, (b1, b2) ->
                        Double.compare(b1.getPrice(), b2.getPrice()));
                break;
            case 3: // Sắp xếp theo giá giảm dần
                Collections.sort(filteredBooks, (b1, b2) ->
                        Double.compare(b2.getPrice(), b1.getPrice()));
                break;
        }
        bookAdapter.notifyDataSetChanged();
    }

    private void filterByCategory(int categoryPosition) {
        if (allBooks.isEmpty()) return;

        filteredBooks.clear();
        String selectedCategory = categoryList.get(categoryPosition).getId();

        if (selectedCategory.equals("All")) {
            filteredBooks.addAll(allBooks);
        } else {
            for (Book book : allBooks) {
                Map<String, Boolean> categories = book.getCategories();
                if (categories != null && categories.containsKey(selectedCategory) && Boolean.TRUE.equals(categories.get(selectedCategory))) {
                    filteredBooks.add(book);
                }
            }
        }

        updateUIAfterFilter();
    }

    private void filterBooksImmediately(String query) {
        // Loại bỏ bất kỳ debounce pending nào
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Thực hiện filter ngay lập tức
        filterBooks(query);
    }

    private void filterBooks(String query) {
        filteredBooks.clear();
        if (query.isEmpty()) {
            filteredBooks.addAll(allBooks);
            tvResultCount.setVisibility(View.GONE);
        } else {
            // Loại bỏ dấu và chuyển về chữ thường để tìm kiếm chính xác hơn
            String normalizedQuery = removeAccent(query.toLowerCase().trim());

            // Hỗ trợ tìm kiếm đa từ
            String[] queryWords = normalizedQuery.split("\\s+");

            for (Book book : allBooks) {
                String normalizedTitle = removeAccent(book.getTitle().toLowerCase());
                String normalizedAuthor = removeAccent(book.getAuthor().toLowerCase());
                String normalizedDescription = removeAccent(book.getDescription().toLowerCase());

                boolean matchAllWords = true;
                for (String word : queryWords) {
                    if (!(normalizedTitle.contains(word) ||
                            normalizedAuthor.contains(word) ||
                            normalizedDescription.contains(word))) {
                        matchAllWords = false;
                        break;
                    }
                }

                if (matchAllWords) {
                    filteredBooks.add(book);
                }
            }

            // Cập nhật số lượng kết quả
            tvResultCount.setVisibility(View.VISIBLE);
            tvResultCount.setText(String.format("Tìm thấy %d kết quả", filteredBooks.size()));
        }

        updateUIAfterFilter();
    }



    private void updateUIAfterFilter() {
        if (filteredBooks.isEmpty()) {
            rcvSearchResults.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rcvSearchResults.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }

        bookAdapter.notifyDataSetChanged();
    }

    private void updateCategorySpinner() {
        // Tạo danh sách tên category để đưa vào Spinner
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }

        // Tạo adapter cho Spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    // Phương thức loại bỏ dấu tiếng Việt
    private String removeAccent(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}