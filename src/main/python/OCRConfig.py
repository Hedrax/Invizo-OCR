#achnoledgement:
#I'm not the author to this one
#It's a bit obvious by the patterns of the code
import os
import logging


class OCRConfig:
    # """Configuration settings for the OCR project."""

    IMAGE_WIDTH = 1024
    IMAGE_HEIGHT = 64

    labels_dir = ['./project_labels']

    # # --- Printed Data Paths ---
    # PRINTED_TEST_IMAGE_DIR = os.path.normpath(os.path.join(OUTPUT_DIR, 'test', 'images'))
    # PRINTED_TEST_LABEL_DIR = os.path.normpath(os.path.join(OUTPUT_DIR, 'test', 'labels'))

    # # --- Handwritten Data Paths ---
    # HANDWRITTEN_DATASET_DIR = os.path.abspath('./Processed1Data')

    # HANDWRITTEN_TEST_IMAGE_DIR = os.path.normpath(os.path.join(HANDWRITTEN_DATASET_DIR, 'test_images'))
    # HANDWRITTEN_TEST_LABEL_DIR = os.path.normpath(os.path.join(HANDWRITTEN_DATASET_DIR, 'test_labels'))

    # # --- Combined Data Paths ---
    # COMBINED_DATASET_DIR = os.path.abspath('finaldataset_combined')

    # TRAIN_COMBINED_IMAGE_DIR = os.path.normpath(os.path.join(COMBINED_DATASET_DIR, 'train_combined', 'images'))
    # TRAIN_COMBINED_LABEL_DIR = os.path.normpath(os.path.join(COMBINED_DATASET_DIR, 'train_combined', 'labels'))

    # VAL_COMBINED_IMAGE_DIR = os.path.normpath(os.path.join(COMBINED_DATASET_DIR, 'val_combined', 'images'))
    # VAL_COMBINED_LABEL_DIR = os.path.normpath(os.path.join(COMBINED_DATASET_DIR, 'val_combined', 'labels'))

    # # --- Folder of Testing Path ---
    # LINES_SAMPLE_DIR = os.path.abspath('./lines sample')  

    # # --- Preprocessed Samples of Testing Path ---
    # PREPROCESSED_SAMPLES_DIR = os.path.abspath('./preprocessed_samples')  


    def extract_characters(labels_file_paths, ALLOWED_CHARACTERS):
        """
        Extracts unique characters from multiple labels files to dynamically
        build the characters list, filtering only allowed characters.
        
        Args:
            labels_file_paths (list): List of label file paths.
        
        Returns:
            list: Sorted list of unique characters.
        """
        characters = set()
        supported_encodings = ['utf-8', 'utf-8-sig', 'utf-16le', 'utf-16be', 'latin1']

        try:
            logging.info("Extracting unique characters from label files...")
            for labels_file_path in labels_file_paths:
                file_decoded = False
                for encoding in supported_encodings:
                    try:
                        with open(labels_file_path, 'r', encoding=encoding) as file:
                            for line_num, line in enumerate(file, 1):
                                label = line.strip().replace('\n', '')
                                if not label:
                                    logging.warning(f"Empty line {line_num} in {labels_file_path}. Skipping.")
                                    continue
                                filtered_label = ''.join([char for char in label if char in ALLOWED_CHARACTERS])
                                if not filtered_label:
                                    logging.warning(f"No valid characters in line {line_num} of {labels_file_path}. Skipping.")
                                    continue
                                characters.update(filtered_label)
                        file_decoded = True
                        break  
                    except UnicodeDecodeError:
                        logging.warning(f"Encoding '{encoding}' failed for '{labels_file_path}'. Trying next encoding.")
                    except Exception as e:
                        logging.error(f"Unexpected error while reading '{labels_file_path}' with encoding '{encoding}': {e}")
                        break  
                if not file_decoded:
                    logging.error(f"Failed to decode '{labels_file_path}' with all supported encodings. Skipping this file.")
            if not characters:
                raise ValueError("No characters found in labels.")
            characters.add(' ')  
            logging.info(f"Extracted {len(characters)} unique characters from all label files.")
        except FileNotFoundError as e:
            logging.error(f"Error: {e}. Cannot extract characters from labels.")
            raise
        except Exception as e:
            logging.error(f"Error while extracting characters: {e}")
            raise
        return sorted(list(characters))

    # PRETRAINED_MODEL_PATH = os.path.abspath('./weights_aug/on_hwr_model_epoch_45.pth')  
    
    EMBED_DIM = 256
    NHEAD = 8
    NUM_ENCODER_LAYERS = 6
    NUM_DECODER_LAYERS = 6
    DIM_FEEDFORWARD = 512
    DROPOUT = 0.1

    INIT_LEARNING_RATE = 0.0001
    DECAY_STEPS = 10000
    DECAY_RATE = 0.9
    PATIENCE_VALUE = 6
    PATIENCE = 3
    BATCH_SIZE = 16
    EPOCHS = 46
    START_EPOCH = 43
    MAX_LEN = 250

    # Special tokens
    PADDING_TOKEN = 0  # <pad>
    SOS_TOKEN = 1      # <sos>
    EOS_TOKEN = 2      # <eos>
    SPACE_TOKEN = 3    # ' '

    WEIGHTS_SAVE_DIR = os.path.abspath('./weights') 
    NEW_WEIGHTS_SAVE_DIR = os.path.abspath('./weights_aug')  

    FREEZE_CNN = True  

       
    
    ALLOWED_CHARACTERS = set([
        ' ', '!', '"', '%', '(', ')', ',', '.', '/', '،', '؛', '؟',
        'ء', 'آ', 'أ', 'ؤ', 'إ', 'ئ', 'ا', 'ب', 'ة', 'ت', 'ث', 'ج', 'ح',
        'خ', 'د', 'ذ', 'ر', 'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ',
        'ـ', 'ف', 'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'و', 'ى', 'ي', 'ً', 'ٌ',
        'ٍ', 'َ', 'ُ', 'ِ', 'ّ', 'ْ', 'ٔ', '٠', '١', '٢', '٣', '٤', '٥',
        '٦', '٧', '٨', '٩', '٪', '٫', '٬', 'ٱ', 'ٲ', 'پ', 'چ', 'ڌ', 'ژ',
        'ښ', 'ڤ', 'ڨ', 'ک', 'گ', 'ھ', 'ہ', 'ۆ', 'ۇ', 'ی', '۔', 'ە', '\u06dd',
        '‐', '–', '‘', '\ufeff'
    ])

    # labels_file_paths = []
    # for label_dir in labels_dir:
    #     if not os.path.exists(label_dir):
    #         logging.warning(f"Label directory '{label_dir}' does not exist. Skipping.")
    #         continue
    #     for file_name in os.listdir(label_dir):
    #         if file_name.lower().endswith('.txt'):
    #             label_file_path = os.path.join(label_dir, file_name)
    #             labels_file_paths.append(os.path.abspath(label_file_path))
    
    # characters = extract_characters(labels_file_paths, ALLOWED_CHARACTERS)

    
    CHAR_TO_NUM = {
        '<pad>': 0, '<sos>': 1, '<eos>': 2, ' ': 3, '!': 4, '"': 5, '(': 6, ')': 7, ',': 8, '.': 9, '/': 10, '،': 11, '؛': 12, '؟': 13, 'ء': 14,'آ': 15, 'أ': 16, 'ؤ': 17, 'إ': 18, 'ئ': 19, 'ا': 20, 'ب': 21, 'ة': 22, 'ت': 23, 'ث': 24, 'ج': 25, 'ح': 26, 'خ': 27, 'د': 28, 'ذ': 29, 'ر': 30, 'ز': 31, 'س': 32, 'ش': 33, 'ص': 34, 'ض': 35, 'ط': 36, 'ظ': 37, 'ع': 38, 'غ': 39, 'ـ': 40, 'ف': 41, 'ق': 42, 'ك': 43, 'ل': 44, 'م': 45, 'ن': 46, 'ه': 47, 'و': 48, 'ى': 49, 'ي': 50, 'ً': 51, 'ٌ': 52, 'ٍ': 53, 'َ': 54, 'ُ': 55, 'ِ': 56, 'ّ': 57, 'ْ': 58, 'ٔ': 59, '٠': 60, '١': 61, '٢': 62, '٣': 63, '٤': 64, '٥': 65, '٦': 66, '٧': 67, '٨': 68, '٩': 69, '٪': 70, '٫': 71, '٬': 72, 'ٱ': 73, 'ٲ': 74, 'پ': 75, 'چ': 76, 'ڌ': 77, 'ښ': 78, 'ڤ': 79, 'ڨ': 80, 'ک': 81, 'گ': 82, 'ھ': 83, 'ۇ': 84, 'ی': 85, '۔': 86, 'ە': 87, '\u06dd': 88, '‐': 89, '–': 90, '\ufeff': 91}
    # idx = 4
    # for char in characters:
    #     if char != ' ' and char not in CHAR_TO_NUM:
    #         CHAR_TO_NUM[char] = idx
    #         idx += 1

    # print(CHAR_TO_NUM)

    
    NUM_TO_CHAR = {idx: char for char, idx in CHAR_TO_NUM.items()}
    

    NUM_TO_CHAR = {idx: char for char, idx in CHAR_TO_NUM.items()}
    @staticmethod
    def calculate_max_len(predefined_max_len=250):
        """
        Set MAX_LEN to 250 as per requirement.
        """
        logging.info(f"Setting MAX_LEN to {predefined_max_len}")
        return predefined_max_len

    @staticmethod
    def count_samples(directory_path):
        """
        Counts the number of samples in a given directory.
        Assumes each .png file represents one sample.
        
        Args:
            directory_path (str): Path to the directory.
        
        Returns:
            int: Number of sample images.
        """
        if not os.path.exists(directory_path):
            logging.warning(f"Directory '{directory_path}' does not exist.")
            return 0
        sample_count = len([file for file in os.listdir(directory_path) if file.lower().endswith('.png')])
        logging.info(f"Found {sample_count} samples in '{directory_path}'.")
        return sample_count
        
config = OCRConfig()