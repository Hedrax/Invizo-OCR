# I'm not the author to that one as well ;)
import numpy as np
from PIL import Image
import cv2

class OCRModule:
    def __init__(self, config, session):
        self.session = session
        self.SOS_TOKEN = config.SOS_TOKEN
        self.EOS_TOKEN = config.EOS_TOKEN
        self.PADDING_TOKEN = config.PADDING_TOKEN
        self.SPACE_TOKEN = config.SPACE_TOKEN
        self.IMAGE_WIDTH = config.IMAGE_WIDTH
        self.IMAGE_HEIGHT = config.IMAGE_HEIGHT
        self.MAX_LEN = config.MAX_LEN
        
        self.NUM_TO_CHAR = config.NUM_TO_CHAR

        # print("[INFO] ONNX Model Inputs:")
        # for input_tensor in self.session.get_inputs():
        #     print(f"  Name: {input_tensor.name}, Shape: {input_tensor.shape}, Type: {input_tensor.type}")

        # print("[INFO] ONNX Model Outputs:")
        # for output_tensor in self.session.get_outputs():
        #     print(f"  Name: {output_tensor.name}, Shape: {output_tensor.shape}, Type: {output_tensor.type}")

    def greedy_decode(self, src, max_len=250):
        batch_size = src.shape[0]
        tgt = np.zeros((batch_size, 1), dtype=np.int64) 
        tgt[:, 0] = self.SOS_TOKEN

        predictions = [[] for _ in range(batch_size)]

        for step in range(max_len):
            # print(f"[INFO] Step {step}: tgt shape = {tgt.shape}, src shape = {src.shape}")

            tgt_expanded = np.pad(tgt, ((0, 0), (0, max_len - tgt.shape[1])), mode='constant', constant_values=self.PADDING_TOKEN)

            inputs = {
                self.session.get_inputs()[0].name: src, 
                self.session.get_inputs()[1].name: tgt_expanded,  
            }

            try:
                outputs = self.session.run(None, inputs)
                # print(f"[INFO] Step {step}: Outputs shape: {outputs[0].shape}")

                last_token_logits = outputs[0][:, step, :]
                next_token = last_token_logits.argmax(axis=-1)

                # print(f"[INFO] Step {step}: Next token = {next_token}")

                for i in range(batch_size):
                    predictions[i].append(next_token[i])

                if all(next_token[i] == self.EOS_TOKEN for i in range(batch_size)):
                    # print(f"[INFO] EOS token reached at step {step}")
                    break

                tgt = np.concatenate([tgt, next_token[:, None]], axis=1)

            except Exception as e:
                # print(f"[ERROR] Error during inference at step {step}: {e}")
                break

        # print(f"[INFO] Final predictions: {predictions}")
        return predictions

    def decode_predictions(self, predictions, num_to_char):
        """
        Decode the predictions from the model.
        """
        decoded = []
        for idx in predictions:
            if idx == self.EOS_TOKEN:
                break
            if idx in [self.PADDING_TOKEN, self.SOS_TOKEN]:
                continue
            elif idx == self.SPACE_TOKEN:
                decoded.append(' ')
            elif idx in num_to_char:
                decoded.append(num_to_char[idx])
            else:
                decoded.append('')
        return ''.join(decoded)
            

    def predict(self, cvImage):
        # try:
            # Load and preprocess image
            # print(f"[INFO] Loading image from: {image_path}")
            # image = Image.open(image_path).convert('RGB')
            # print(f"[INFO] Original image size: {image.size}")
            
            cvImage_2 = cv2.cvtColor(cvImage, cv2.COLOR_BGR2RGB)
            
            # Convert the RGB image to a PIL image
            image = Image.fromarray(cvImage_2)

            image = image.resize((self.IMAGE_WIDTH, self.IMAGE_HEIGHT))  
            # print(f"[INFO] Resized image size: {image.size}")

            image = np.array(image).transpose(2, 0, 1)
            image = image.astype(np.float32) / 255.0
            image = np.expand_dims(image, axis=0)
            # print(f"[INFO] Final preprocessed image shape: {image.shape}")

            token_indices = self.greedy_decode(image, max_len=self.MAX_LEN)
            return self.decode_predictions(token_indices[0], self.NUM_TO_CHAR)

        # except Exception as e:
        #     print("[ERROR] Prediction failed:", e)
        #     e.printstack()
        #     return ""
