# Disease Prediction Application

## Introduction
<p>Disease prediction application (DPA) is developed to support patients to receive remote diagnosis, thus, reduce the need to go to hospitals and clinics which will reduce the risk of being infected with infectious diseases, especially in the context of the COVID-19 epidemic. At the same time, it helps to save time and effort of patients and doctors when there is no need to directly examine minor illnesses such as common cold, sore throat and rhinitis, etc.</p>
<p>This project comprises of two main parts: the application and the disease prediction model. </p>
<p>In the disease prediction model, we use the following technologies to achieve high accuracy prediction result:  Tokenizer, Synonym Tree, and Multi Class Text Classification Model using Deep Learning.</p>
<p>First, we use <b>Tokenizer</b> to split text into keywords. Tokenization is the process of turning sensitive data into non-sensitive data called "tokens" that can be used in a database or internal system without bringing it into scope. Tokenization can be used to secure sensitive data by replacing the original data with an unrelated value of the same length and format. From there, the keywords will be put into the Synonym Tree.</p>
<p>A <b>Synonym Tree</b> is a search for words with common meanings for user descriptions, allowing the word to be changed into a medical term that the chatbot can understand. It analyses the words in the sentence and identifies the user's disease description languages, then separates the descriptive languages and translates into medical symptoms. For example, when a user enters the symptom " họng" after using it, it will turn into "đau họng" and "sore throat" will be recorded by the chatbot and converted into a list of symptoms to predict the disease.</p>
<p><b>Multi Class Text Classification Model using Deep Learning</b> is a text classification model using TensorFlow Deep Learning library to classify disease based on user input content. After the data normalize step, user input will be pre-processed and converted into a 2D integer array. Then the pre-processed data will be put into our Classification model, process and give result as a 2D float array. Using the result, the app will display the most possible disease user may have.</p>
<p>In the application, we decided to build our app on <b>Android Operating System</b> because it has the largest market share which means our app can reach the most number of people possible. The app will be a “native“ app – which is built using the SDK provided by Google, for the best performance and the most compatibility. About the back-end of the app, we use <b>Firebase</b> to build a real time database, create authentication connections that allow login to android apps via google or phone number. Firebase is a Backend-as-a-Service (Baas). It provides developers with a variety of tools and services to help them develop quality apps, grows their user base, and earn profits. It is built on top of Google's infrastructure.  Firebase is classified as a NoSQL database program that stores data in JSON-like documents.</p>

## System Architecture
### Overall architecture
![Overall architecture of the system](/img/overall-architecture-system.png "Overall architecture of the system")

### The overall architecture of The Disease Prediction Model
![Overall architecture of the Disease Prediction Model](/img/overall-architecture-disease-prediction-model.png "Overall architecture of the Disease Prediction Model")

## Basic functions, Limitations & Restrictions
### Basic functions
FE-01:	Sign in/sign up with Phone Number or Google account.<br>
FE-02:	Custom user interface for different role.<br>
FE-03: Patient can chat with chat bot and get prediction.<br>
FE-04: Patient can contact and chat with doctor after doctor confirmed prediction.<br>
FE-05: Patient can view their predictions in predictions history.<br>
FE-06: Patient/doctor can view a chat session in chat list history.<br>
FE-07: Doctor can view a prediction in list prediction pending.<br>
FE-08: Doctor can confirm predictions belong to his/her specialization.<br>
FE-09: Doctor can review the predictions he/she has confirmed in the list prediction history.<br>

### Limitations
LI-1:	The system can only give predictions with limited accuracy and for reference purposes only. Please contact your doctor for the most accurate results.<br>
LI-2: 	DPA can only predict 9 following diseases: Malaria, Influenza, Pneumonia, Mumps, Acute Diarrheal, Pharyngitis, Peptic Ulcer, Myocardial Infarction, Gastroesophageal Reflux. Each disease in the list has different prediction accuracy from our system.<br>
LI-3: 	DPA only provide sign-in method with phone number and Google account.<br>
LI-4: 	Support Android 8.1 Oreo or later.<br>
LI-5:	DPA only support Vietnam region.<br>
LI-6: 	Although we tried our best to support as many phones as possible, there are still a huge number of different types of phones on the market so maybe some phones are not compatible with our app.<br>
LI-7:	DPA can only give prediction of one disease at a time.

### Restrictions
RE-1:	Internet connection is recommended.<br>
RE-2:	User is recommended to enter the symptoms correctly for an accurate diagnosis.

## System Requirements
- Mobile phones using Android 8.1 Oreo or later. 
- Available storage space > 100MB.

## How to run
- Create your Firebase project with Phone authentication and Google Sign-in
- Replace our "google-services.json" file with yours
- Build and run the project

## Demo
https://www.youtube.com/watch?v=3RW_zfiLpbk


