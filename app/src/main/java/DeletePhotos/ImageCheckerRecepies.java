package DeletePhotos;

import static android.content.ContentValues.TAG;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageCheckerRecepies {

    private static final long CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(1); // Intervalo de comprobación (3 segundos)
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private CountDownTimer timer;

    public ImageCheckerRecepies() {
        // Inicializar Firebase Firestore y Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void start() {
        startTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(CHECK_INTERVAL, CHECK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // No se utiliza en este ejemplo
            }

            @Override
            public void onFinish() {
                // Realizar la comprobación de imágenes
                checkImages();
                // Reiniciar el cronómetro
                startTimer();
            }
        }.start();
    }

    private void checkImages() {
        // Obtener todas las imágenes en el almacenamiento de Firebase
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("imagesRecetas");

        // Obtener la lista de imágenes
        getListOfImages(imagesRef);
    }

    private void getListOfImages(StorageReference reference) {
        reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // Lista de referencias de imágenes en el almacenamiento
                List<StorageReference> imageReferences = listResult.getItems();

                // Verificar las imágenes con Firestore
                checkImagesWithFirestoreProducts(imageReferences);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error al obtener la lista de imágenes", e);
            }
        });
    }

    private void checkImagesWithFirestoreProducts(final List<StorageReference> imageReferences) {
        db.collection("recetas")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Obtener la lista de nombres de imágenes en Firestore
                        List<String> imageNames = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String imageUrl = documentSnapshot.getString("imagenUrl");
                            if (imageUrl != null) {
                                String imageName = getImageNameFromUrl(imageUrl);
                                imageNames.add(imageName);
                            }
                        }

                        // Verificar las imágenes en el almacenamiento
                        for (StorageReference imageRef : imageReferences) {
                            String imageName = imageRef.getName();

                            if (!imageNames.contains(imageName)) {
                                deleteImage(imageRef);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al obtener la lista de documentos de Firestore", e);
                    }
                });
    }

    private String getImageNameFromUrl(String imageUrl) {
        int startIndex = imageUrl.indexOf("imagesRecetas%2F") + 16 ;
        int endIndex = imageUrl.indexOf("?alt");

        if (startIndex >= 0 && endIndex >= 0) {
            return imageUrl.substring(startIndex, endIndex);
        }

        return null;
    }

    private void deleteImage(StorageReference imageRef) {
        // Eliminar la imagen del almacenamiento de Firebase
        imageRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Imagen eliminada correctamente");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al eliminar la imagen", e);
                    }
                });
    }
}




