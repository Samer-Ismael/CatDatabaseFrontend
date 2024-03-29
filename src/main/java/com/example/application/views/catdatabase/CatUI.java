package com.example.application.views.catdatabase;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Route("")
public class CatUI extends VerticalLayout {

    private final String backendUrl;
    private final Grid<Cat> catGrid = new Grid<>(Cat.class);
    private final TextField name = new TextField("Name");
    private final TextField color = new TextField("Color");
    private final TextField age = new TextField("Age");


    public CatUI(ServerUrl serverUrl) {
        this.backendUrl = serverUrl.getUrl();


        catGrid.setColumns("id", "name", "color", "age");

        Image image = getImage();

        Span serverMessageSpan = new Span();
        serverMessageSpan.getStyle()
                .set("text-align", "center")
                .set("font-size", "20px")  // Adjust font size as needed
                .set("font-family", "Arial, sans-serif"); // Specify font family for better appearance

        serverMessageSpan.getStyle().set("text-align", "center");

        VerticalLayout imageLayout = new VerticalLayout(image, serverMessageSpan); // Use VerticalLayout instead of HorizontalLayout
        imageLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        HorizontalLayout inputLayout = new HorizontalLayout(name, color, age);
        inputLayout.setSpacing(true);

        Button addCatButton = new Button("Add Cat");
        Button updateCatButton = new Button("Update Cat");
        Button deleteCatButton = new Button("Delete Cat");
        Button swaggerButton = new Button("Swagger Documentation");
        Button clearButton = new Button("Clear Fields");
        HorizontalLayout buttonLayout = new HorizontalLayout(addCatButton, updateCatButton, deleteCatButton, clearButton, swaggerButton);
        buttonLayout.setSpacing(true);

        addListenerToButtons(addCatButton, updateCatButton, deleteCatButton, swaggerButton, clearButton);

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(imageLayout, inputLayout, buttonLayout, catGrid);

        String message = serverMessage();
        serverMessageSpan.setText(message);

        if (!isServerAvailable()) {
            showServerDownNotification();
        } else {
            refreshGrid();
        }
        catGrid.asSingleSelect().addValueChangeListener(event -> {
            Cat selectedCat = event.getValue();
            populateFieldsWithSelectedCatInfo(selectedCat);
        });
    }

    private String serverMessage() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Make a GET request to the server endpoint
            return restTemplate.getForObject(backendUrl + "/server", String.class);
        } catch (Exception e) {
            // Handle any exceptions
            return "";
        }
    }

    private void addListenerToButtons(Button addCatButton, Button updateCatButton, Button deleteCatButton, Button swaggerButton, Button clearButton) {
        addCatButton.addClickListener(e -> addCat());
        updateCatButton.addClickListener(e -> updateCat());
        deleteCatButton.addClickListener(e -> deleteCat());
        swaggerButton.addClickListener(e -> navigateToSwaggerUi());
        clearButton.addClickListener(e -> clearFields());
    }

    private Image getImage() {
        Image image = new Image(new StreamResource("cat.jpg", () -> getClass().getResourceAsStream("/images/cat.jpg")), "Cat Image");
        image.getElement().getStyle().set("width", "250px");
        image.getElement().getStyle().set("height", "250px");
        return image;
    }

    private void clearFields() {
        // Clear text fields
        name.clear();
        color.clear();
        age.clear();
    }
    private void navigateToSwaggerUi() {
        UI.getCurrent()
                .getPage()
                .setLocation(backendUrl + "/swagger-ui/index.html");
    }

    private void addCat() {
        try {
            String catName = name.getValue();
            String catColor = color.getValue();
            String catAge = age.getValue();

            Cat cat = new Cat();
            cat.setName(catName);
            cat.setColor(catColor);
            cat.setAge(Integer.parseInt(catAge));

            ResponseEntity<Void> responseEntity = new RestTemplate().postForEntity(
                    backendUrl + "/cat/",
                    cat,
                    Void.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                refreshGrid();
                Notification.show("Cat added successfully", 3000, Notification.Position.TOP_CENTER);
            } else {
                Notification.show("Failed to add cat: " + responseEntity.getStatusCode(), 3000, Notification.Position.TOP_CENTER);
            }
        } catch (NumberFormatException e) {
            Notification.show("Something went wrong.", 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void populateFieldsWithSelectedCatInfo(Cat selectedCat) {
        if (selectedCat != null) {
            name.setValue(selectedCat.getName());
            color.setValue(selectedCat.getColor());
            age.setValue(String.valueOf(selectedCat.getAge()));
        } else {
            // If no cat is selected, clear the text fields
            name.clear();
            color.clear();
            age.clear();
        }
    }

    private void updateCat() {
        Cat selectedCat = catGrid.asSingleSelect().getValue();
        if (selectedCat != null) {
            try {

                // Update the selected cat's properties with the values from the text fields
                selectedCat.setName(name.getValue());
                selectedCat.setColor(color.getValue());
                selectedCat.setAge(Integer.parseInt(age.getValue()));

                ResponseEntity<Void> responseEntity = new RestTemplate().exchange(
                        backendUrl + "/cat/" + selectedCat.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(selectedCat),
                        Void.class
                );

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    refreshGrid();
                    Notification.show("Cat updated successfully", 3000, Notification.Position.TOP_CENTER);
                } else {
                    Notification.show("Failed to update cat: " + responseEntity.getStatusCode(), 3000, Notification.Position.TOP_CENTER);
                }
            } catch (NumberFormatException e) {
                Notification.show("Please enter a valid age.", 3000, Notification.Position.TOP_CENTER);
            }
        } else {
            Notification.show("Select a cat to update", 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void deleteCat() {
        Cat selectedCat = catGrid.asSingleSelect().getValue();
        if (selectedCat != null) {
            ResponseEntity<Void> responseEntity = new RestTemplate().exchange(
                    backendUrl + "/cat/" + selectedCat.getId(),
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                refreshGrid();
                Notification.show("Cat deleted successfully", 3000, Notification.Position.TOP_CENTER);
            } else {
                Notification.show("Failed to delete cat: " + responseEntity.getStatusCode(), 3000, Notification.Position.TOP_CENTER);
            }
        } else {
            Notification.show("Select a cat to delete", 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void refreshGrid() {
        ResponseEntity<List<Cat>> responseEntity = new RestTemplate().exchange(
                backendUrl + "/cat/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            List<Cat> cats = responseEntity.getBody();
            catGrid.setItems(cats);
            catGrid.deselectAll();
        } else {
            Notification.show("Error fetching cat data: " + responseEntity.getStatusCode());
        }
    }

    private boolean isServerAvailable() {
        try {
            URL url = new URL(backendUrl + "/server");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000);

            int responseCode = connection.getResponseCode();

            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private void showServerDownNotification() {
        Notification.show("Server is down. Please try again later.", 0, Notification.Position.MIDDLE);
    }
}