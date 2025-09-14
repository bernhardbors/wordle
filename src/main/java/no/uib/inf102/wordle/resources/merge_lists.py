import os 
__location__ = os.path.realpath(
    os.path.join(os.getcwd(), os.path.dirname(__file__)))

with open(__location__+"/guessWords.txt", "r") as f1:
    with open(__location__+"/answerWords.txt", "r") as f2:
        with open(__location__+"/allWords.txt", "w") as f3:
            ls = []
            for word in f1.readlines():
                ls.append(word)
            for word in f2.readlines():
                if word not in ls:
                    ls.append(word)
            for line in ls:
                f3.write(f"{line}")