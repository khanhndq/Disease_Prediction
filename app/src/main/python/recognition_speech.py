import numpy as np
import operator
import pandas as pd
import warnings
from RDRSegmenter import RDRSegmenter
from Tokenizer import Tokenizer
from collections import Counter
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split, cross_val_score

warnings.simplefilter("ignore")
from os.path import dirname, join

filename = join(dirname(__file__), "filename.txt")
# nltk.download('all')
filename = join(dirname(__file__), "Disease_comb.csv")
filename2 = join(dirname(__file__), "Disease_norm.csv")
df_comb = pd.read_csv(filename)  # Disease combination
df_norm = pd.read_csv(filename2)  # Individual Disease

X = df_comb.drop(['label_dis'], axis=1)
Y = df_comb.label_dis

X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.25,
                                                    random_state=56)
# # define the model
model = RandomForestClassifier()
model.fit(X_train, y_train)
scores = cross_val_score(model, X_test, y_test, cv=3)
# print("score random forest : " + str(model.score(X_test, y_test)))
X = df_norm.iloc[:, 1:]
Y = df_norm.iloc[:, 0:1]

# # List of symptoms
dataset_symptoms = list(X.columns)


def get_key_word(input):
    input = input.lower()
    cols = df_comb.columns[1:]
    get_symptoms = []
    for i in cols:
        get_symptoms.append(i)
    rdrsegment = RDRSegmenter()
    tokenizer = Tokenizer()
    input = input.replace(",", "")
    output = rdrsegment.segmentRawSentences(tokenizer, input)
    output = output.split()
    sub_string = []
    for substr in output:
        sub_string.append(substr.replace("_", " "))
    get_symptoms_in_text = []
    for i in sub_string:
        for j in get_symptoms:
            if j == i:
                get_symptoms_in_text.append(i)
    # print(get_symptoms_in_text)
    return get_symptoms_in_text


def input_rec(input):
    # Taking symptoms from user as input
    user_symptoms = input

    # Preprocessing the input symptoms
    processed_user_symptoms = get_key_word(user_symptoms)
    # Loop over all the symptoms in dataset and check its similarity score to the synonym string of the user-input
    # symptoms. If similarity>0.5, add the symptom to the final list
    found_symptoms = set()
    for idx, data_sym in enumerate(processed_user_symptoms):
        data_sym_split = data_sym.split()
        for user_sym in processed_user_symptoms:
            count = 0
            for symp in data_sym_split:
                if symp in user_sym.split():
                    count += 1
            if count / len(data_sym_split) > 0.5:
                found_symptoms.add(data_sym)
    found_symptoms = list(found_symptoms)
    return found_symptoms


# confirm symtoms
def confirmSymtoms(found_symptoms):
    # Find other relevant symptoms from the dataset based on user symptoms based on the highest co-occurance with the
    # ones that is input by the user
    dis_list = set()
    final_symp = []
    counter_list = []
    for symp in found_symptoms:
        final_symp.append(symp)
        dis_list.update(set(df_norm[df_norm[symp] == 1]['label_dis']))

    for dis in dis_list:
        row = df_norm.loc[df_norm['label_dis'] == dis].values.tolist()
        row[0].pop(0)
        for idx, val in enumerate(row[0]):
            if val != 0 and dataset_symptoms[idx] not in final_symp:
                counter_list.append(dataset_symptoms[idx])

    # Symptoms that co-occur with the ones selected by user
    dict_symp = dict(Counter(counter_list))
    dict_symp_tup = sorted(dict_symp.items(), key=operator.itemgetter(1), reverse=True)
    return dict_symp_tup

def otherSymtoms(dict_symp_tup,select_list ):
    # Iteratively, suggest top co-occuring symptoms to the user and ask to select the ones applicable
    found_symptoms = []
    count = 0
    for tup in dict_symp_tup:
        count += 1
        found_symptoms.append(tup[0])
        if count % 5 == 0 or count == len(dict_symp_tup):
            print("\nBạn có bị các triệu chứng khác theo gợi ý không:")
            for idx, ele in enumerate(found_symptoms):
                print(idx, ":", ele)
            if select_list[0] == 'không':
                break
            if select_list[0] == 'skip':
                found_symptoms = []

                continue
            for idx in select_list:
                final_symp.append(found_symptoms[int(idx)])
            found_symptoms = []

    # Create query vector based on symptoms selected by the user
    print("\nDanh sách triệu chứng bệnh dùng để dự đoán:")
    sample_x = [0 for x in range(0, len(dataset_symptoms))]
    for val in final_symp:
        print(val)
        sample_x[dataset_symptoms.index(val)] = 1
    rs = np.array(final_symp)
    return rs
